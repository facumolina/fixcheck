package org.imdea.fixcheck.transform.common;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import org.imdea.fixcheck.prefix.Input;

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
      if (type.equals(StringLiteralExpr.class)) {
        StringLiteralExpr stringLiteralExpr = (StringLiteralExpr) expression;
        // Remove quotes from value
        stringLiteralExpr.setString(value.toString().substring(1, value.toString().length() - 1));
        return;
      }
      throw new IllegalArgumentException("Don't know how to replace basic expression of type " + expression.getClass().getName());
    } else {
      Node parent = expression.getParentNode().get();
      if (expression.getClass().equals(ObjectCreationExpr.class)) {
        // In the original expression, an object is created. Thus, we need to replace the whole expression
        ObjectCreationExpr newObjectCreationExpr = buildObjectCreationExpr(type, value);
        parent.replace(expression, newObjectCreationExpr);
        return;
      }
      throw new IllegalArgumentException("Don't know how to replace non basic expression of type " + expression.getClass().getName());
    }


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

}
