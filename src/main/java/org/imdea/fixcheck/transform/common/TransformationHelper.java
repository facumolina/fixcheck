package org.imdea.fixcheck.transform.common;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import org.imdea.fixcheck.prefix.Input;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TransformationHelper class: helper class for applying transformations.
 */
public class TransformationHelper {

  // Classes to be ignored when applying transformations
  // They are ignored because changing their inputs may cause the program to crash
  private static final String[] ignoreClasses = {
      "java.io.File",
      "java.io.FileInputStream",
      "java.io.FileOutputStream",
      "java.io.FileReader",
      "java.io.FileWriter",
      "java.nio.file.Files",
      "java.nio.file.Path",
  };

  /**
   * Initialize a transformed class from a class declaration and a method declaration
   */
  public static CompilationUnit initializeTransformedClass(String newClassName, CompilationUnit compilationUnit) {
    CompilationUnit newCompilationUnit = compilationUnit.clone();
    String className = newCompilationUnit.getPrimaryTypeName().get();
    Optional<ClassOrInterfaceDeclaration> classDeclarationOpt = newCompilationUnit.getClassByName(className);
    classDeclarationOpt.get().setName(newClassName);
    return newCompilationUnit;
  }

  /**
   * Get the method declaration of a method in a class.
   */
  public static MethodDeclaration getMethodDeclFromCompilationUnit(CompilationUnit compilationUnit, String methodName) {
    // Find the method declaration which name is methodName
    return compilationUnit.findAll(MethodDeclaration.class).stream()
        .filter(methodDeclaration -> methodDeclaration.getNameAsString().equals(methodName))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Method " + methodName + " not found in class " + compilationUnit.getPrimaryTypeName().get()));
  }

  /**
   * Initialize a transformed class from a soot class.
   * @param newClassName is the name of the new class.
   * @param sootClass is the soot class to initialize the new class from.
   * @return the new transformed class.
   */
  public static SootClass initializeTransformedClass(String newClassName, SootClass sootClass) {
    SootClass transformedClass = new SootClass(newClassName, sootClass.getModifiers(), sootClass.moduleName);
    transformedClass.setSuperclass(sootClass.getSuperclass());
    // TODO: We should be copying the fields here!, but the problem is that
    // we would need to create the corresponding locals in the body of the
    // init method and others. For now, we just copy the init method.
    
    // Replicate the init method
    SootMethod initMethod = sootClass.getMethodByName("<init>");
    Body initMethodBody = initMethod.retrieveActiveBody();
    SootMethod newInitMethod = new SootMethod("<init>", initMethod.getParameterTypes(), initMethod.getReturnType(), initMethod.getModifiers());
    newInitMethod.addAllTagsOf(initMethod);
    Body newInitMethodBody = (Body)initMethodBody.clone();
    newInitMethod.setActiveBody(newInitMethodBody);
    transformedClass.addMethod(newInitMethod);
    return transformedClass;
  }

  /**
   * Return the index of a given local in the list of parameters of the method call that uses it.
   * @param stmt Invoke statement to search
   * @param value Value to search
   * @return Index of the local in the list of parameters
   */
  private static int getIndexForValue(JInvokeStmt stmt, Value value) {
    int index = 0;
    List<Value> args = stmt.getInvokeExpr().getArgs();
    for (Value arg : args) {
      if (arg.equals(value)) return index;
      index++;
    }
    throw new IllegalArgumentException("Unable to find index for Value " + value + ". Is it used?");
  }

  /**
   * Returns true if the local is used within the body
   */
  public static boolean isLocalUsed(Local local, Body body) {
    List<Unit> units = getUnitsUsingLocal(local, body);
    return !units.isEmpty();
  }

  /**
   * Return the list of units that use a given local, without including the definition of the local
   * @param local Local to search
   * @param body Body to search
   * @return List of Units that use the given local, empty if not found
   */
  public static List<Unit> getUnitsUsingLocal(Local local, Body body) {
    List<Unit> units = new ArrayList<>();
    for (Unit ut : body.getUnits()) {
      for (ValueBox vb : ut.getUseBoxes()) {
        if (vb.getValue().equals(local)) {
          if (ut instanceof JInvokeStmt) {
            JInvokeStmt stmt = ((JInvokeStmt) ut);
            if (isConstructorCall(stmt, local)) continue;
            if (isIgnoredClass(stmt.getInvokeExpr())) {
              System.out.println("Ignoring class: "+stmt.getInvokeExpr().getMethod().getDeclaringClass().getName());
              continue;
            }
          }
          units.add(ut);
        }
      }
    }
    return units;
  }

  /**
   * Returns the list of units that use a given constant
   * @param value Constant to search
   * @param body Body to search
   * @return List of Units that use the given constant, empty if not found
   */
  public static List<Unit> getUnitsUsingConstant(Value value, Body body) {
    List<Unit> units = new ArrayList<>();
    for (Unit ut : body.getUnits()) {
      for (ValueBox vb : ut.getUseBoxes()) {
        if (vb.getValue().equals(value)) {
          if (ut instanceof JInvokeStmt) {
            JInvokeStmt stmt = ((JInvokeStmt) ut);
            if (isIgnoredClass(stmt.getInvokeExpr())) {
              System.out.println("Ignoring class: "+stmt.getInvokeExpr().getMethod().getDeclaringClass().getName());
              continue;
            }
          }
          if (ut instanceof JAssignStmt) {
            JAssignStmt stmt = ((JAssignStmt) ut);
            if (stmt.containsInvokeExpr() && isIgnoredClass(stmt.getInvokeExpr())) {
              System.out.println("Ignoring class: "+stmt.getInvokeExpr().getMethod().getDeclaringClass().getName());
              continue;
            }
          }
          units.add(ut);
        }
      }
    }
    return units;
  }


  /**
   * Returns true if the invoke statement is a constructor call of the given local
   * @param invokeStmt Invoke statement to check
   * @param value Value to check
   * @return True if the invoke statement is a constructor call
   */
  public static boolean isConstructorCall(JInvokeStmt invokeStmt, Value value) {
    if (invokeStmt.getInvokeExpr() instanceof SpecialInvokeExpr) {
      SpecialInvokeExpr specialInvokeExpr = (SpecialInvokeExpr) invokeStmt.getInvokeExpr();
      if (specialInvokeExpr.getBase().equals(value) && specialInvokeExpr.getMethod().getName().equals("<init>"))
        return true;
    }
    return false;
  }

  /**
   * Returns true if the invoke statement is a method of a class that must be ignored
   * @param invokeExpr Invoke expression to check
   * @return True if the invoke statement is a method of a class that must be ignored
   */
  public static boolean isIgnoredClass(InvokeExpr invokeExpr) {
    for (String ignoredClass : ignoreClasses) {
      if (invokeExpr.getMethod().getDeclaringClass().getName().equals(ignoredClass))
        return true;
    }
    return false;
  }

  public static void replace(Input input, Class<? extends Expression> type, Object value) {
    Expression expression = input.getExpression();
    if (input.isBasic()) {
      if (type.equals(IntegerLiteralExpr.class)) {
        IntegerLiteralExpr integerLiteralExpr = (IntegerLiteralExpr) expression;
        integerLiteralExpr.setValue(value.toString());
        return;
      }
      if (type.equals(LongLiteralExpr.class)) {
        LongLiteralExpr longLiteralExpr = (LongLiteralExpr) expression;
        longLiteralExpr.setValue(value.toString());
        return;
      }
    } else {
      Node parent = expression.getParentNode().get();
      if (expression.getClass().equals(ObjectCreationExpr.class)) {
        // In the original expression, an object is created. Thus, we need to replace the whole expression
        ObjectCreationExpr newObjectCreationExpr = buildObjectCreationExpr(type, value);
        parent.replace(expression, newObjectCreationExpr);
        return;
      }
    }

    throw new IllegalArgumentException("Don't know how to replace expression of type " + expression.getClass().getName());
  }

  private static ObjectCreationExpr buildObjectCreationExpr(Class<? extends Expression> type, Object value) {
    if (type.equals(BooleanLiteralExpr.class)) {
      CompilationUnit cu = StaticJavaParser.parse("class X{void x(){" +
          "new Boolean(" + value + ");" +
          "}}");
      ObjectCreationExpr objectCreationExpr = cu.findFirst(ObjectCreationExpr.class).get();
      return objectCreationExpr;
    }
    if (type.equals(IntegerLiteralExpr.class)) {
      CompilationUnit cu = StaticJavaParser.parse("class X{void x(){" +
          "new Integer(" + value + ");" +
          "}}");
      ObjectCreationExpr objectCreationExpr = cu.findFirst(ObjectCreationExpr.class).get();
      return objectCreationExpr;
    }
    if (type.equals(LongLiteralExpr.class)) {
      CompilationUnit cu = StaticJavaParser.parse("class X{void x(){" +
          "new Long(" + value + ");" +
          "}}");
      ObjectCreationExpr objectCreationExpr = cu.findFirst(ObjectCreationExpr.class).get();
      return objectCreationExpr;
    }
    if (type.equals(StringLiteralExpr.class)) {
      CompilationUnit cu = StaticJavaParser.parse("class X{void x(){" +
          "new String(" + value + ");" +
          "}}");
      ObjectCreationExpr objectCreationExpr = cu.findFirst(ObjectCreationExpr.class).get();
      return objectCreationExpr;
    }
    throw new IllegalArgumentException("Don't know how to create constructor of expression of type" + type.getName());
  }

  /**
   * Replace all uses of v1 in body with v2 ignoring the given unit
   * @param body Body to modify
   * @param v1 Value to replace
   * @param v2 Value to replace with
   * @param unit Unit to ignore
   */
  public static void replaceIgnoring(Body body, Value v1, Value v2, Unit unit) {
    for (Unit ut : body.getUnits()) {
      if (ut.equals(unit)) continue;
      for (ValueBox vb : ut.getUseBoxes())
        if( vb.getValue().equals(v1))
          vb.setValue(v2);
    }
  }

}
