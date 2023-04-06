package org.imdea.fixcheck.transform.input;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import org.imdea.fixcheck.Properties;
import org.imdea.fixcheck.prefix.ConstantInput;
import org.imdea.fixcheck.prefix.Input;
import org.imdea.fixcheck.prefix.LocalInput;
import org.imdea.fixcheck.prefix.Prefix;
import org.imdea.fixcheck.transform.PrefixTransformer;
import org.imdea.fixcheck.transform.common.TransformationHelper;
import soot.*;
import soot.jimple.*;

import java.util.List;
import java.util.Random;

/**
 * Input Transformer class: transform a prefix by changing its 'input'.
 * @author Facundo Molina
 */
public class InputTransformer extends PrefixTransformer {

  private static final String baseClassName = "SimilarPrefixInputTransformer"; // Base name for the new prefix class
  private static final String basePrefixName = "similarPrefix"; // Base name for the new prefix method

  private int transformationsApplied; // Number of transformations applied

  private String lastTransformation; // Last transformation applied

  public InputTransformer() {
    transformationsApplied = 0;
    lastTransformation = "";
  }

  @Override
  public Prefix transform(Prefix prefix) {
    CompilationUnit prefixCompilationUnit = prefix.getMethodCompilationUnit();
    MethodDeclaration prefixMethod = prefix.getMethod();
    // Prepare the new class
    String className = baseClassName + transformationsApplied;
    CompilationUnit newCompilationUnit = TransformationHelper.initializeTransformedClass(className, prefixCompilationUnit);
    // Prepare the new method body
    MethodDeclaration newMethod = TransformationHelper.getMethodDeclFromCompilationUnit(newCompilationUnit, prefixMethod.getNameAsString());
    // Replace the input
    replaceInput(newMethod);
    // Transformed prefix
    Prefix transformedPrefix = new Prefix(newMethod, newCompilationUnit, prefix);
    transformedPrefix.setClassName(className);
    return transformedPrefix;
  }

  @Override
  public String getLastTransformation() {
    return lastTransformation;
  }

  /**
   * Replace a randomly selected input within the method.
   * @param methodDecl is the method declaration to be transformed.
   */
  private void replaceInput(MethodDeclaration methodDecl) {
    if (!methodDecl.getBody().isPresent()) throw new IllegalArgumentException("Method body is not present");
    // Get an input expression to be replaced
    Class<? extends Expression> classToSearch = getClassForInput();
    Expression inputExpr = getRandomInput(methodDecl, classToSearch);
    String previousExpr = inputExpr.toString();
    // Get a value for the new input
    Object value = InputHelper.getValueForType(inputExpr.getClass());
    // Replace the value in the expression
    TransformationHelper.replace(inputExpr, value);
    lastTransformation = "[" + previousExpr + ":" + classToSearch.getSimpleName() +"] replaced by [" + inputExpr + ":" + value.getClass().getSimpleName()+"]";
    transformationsApplied++;
  }

  /**
   * Replace the constructor of the given input with the new statements for assigning the new input and calling the constructor.
   * @param body Body to transform
   * @param inputToReplace Input to replace
   * @param assignStmt Statement for assigning the new input
   * @param constructorInvoke Statement for calling the constructor of the new input
   */
  private void replaceConstructor(Body body, Input inputToReplace, AssignStmt assignStmt, InvokeStmt constructorInvoke) {
    if (inputToReplace instanceof LocalInput) {
      replaceConstructorLocal(body, inputToReplace, assignStmt, constructorInvoke);
    } else if (inputToReplace instanceof ConstantInput) {
      // For constants, just add the new constructor call in the right place
      Unit unit = TransformationHelper.getFirstUnitUsingInput(inputToReplace, body);
      body.getUnits().insertBefore(assignStmt, unit);
      body.getUnits().insertBefore(constructorInvoke, unit);
    } else {
      throw new IllegalArgumentException("Don't know how to replace the constructor for input type: " + inputToReplace.getClass().getName());
    }
  }

  private void replaceConstructorLocal(Body body, Input inputToReplace, AssignStmt assignStmt, InvokeStmt constructorInvoke) {
    // First replace the assignment
    for (Unit ut : body.getUnits()) {
      if (ut instanceof AssignStmt) {
        AssignStmt assign = (AssignStmt) ut;
        if (assign.getLeftOp().equals(inputToReplace.getValue())) {
          body.getUnits().swapWith(ut, assignStmt);
          break;
        }
      }
    }
    // Then replace the actual constructor call
    for (Unit ut : body.getUnits()) {
      if (ut instanceof InvokeStmt) {
        InvokeExpr invokeExpr = ((InvokeStmt)ut).getInvokeExpr();
        boolean classMatch = invokeExpr.getMethod().getDeclaringClass().getName().equals(inputToReplace.getValue().getType().toString());
        boolean methodMatch = invokeExpr.getMethod().getName().equals("<init>");
        if (classMatch && methodMatch) {
          body.getUnits().swapWith(ut, constructorInvoke);
          break;
        }
      }
    }
  }

  /**
   * Get a random Expression for the input class
   * @param methodDecl method to search
   * @return Random Expression for the input class
   */
  private Expression getRandomInput(MethodDeclaration methodDecl, Class<? extends Expression> classToSearch) {
    List<? extends Expression> allInputsOfType = methodDecl.findAll(classToSearch);
    if (allInputsOfType.isEmpty()) throw new IllegalArgumentException("No locals of type " + Properties.INPUTS_CLASS);
    Random random = new Random();
    int index = random.nextInt(allInputsOfType.size());
    return allInputsOfType.get(index);
  }

  /**
   * Define a new local for the given type
   * @param type Type of the local
   * @param body Body to add the local
   * @return New local
   */
  private Local defineLocalForType(Class<?> type, Body body) {
    Local newInput = Jimple.v().newLocal("newInput", RefType.v(type.getName()));
    body.getLocals().add(newInput);
    return newInput;
  }

  /**
   * Get the class for the given type
   * @return Class for the current input type
   */
  private Class<? extends Expression> getClassForInput() {
    if (InputHelper.INPUTS_BY_TYPE.containsKey(Properties.INPUTS_CLASS)) {
      List<Class<? extends Expression>> possibleInputs = InputHelper.INPUTS_BY_TYPE.get(Properties.INPUTS_CLASS);
      Random random = new Random();
      int index = random.nextInt(possibleInputs.size());
      return possibleInputs.get(index);
    }
    throw new IllegalArgumentException("Input type not supported: " + Properties.INPUTS_CLASS);
  }

  /**
   * Generate the constructor for the given type
   * @param input Local to use in the constructor
   * @param type Type of the constructor
   * @param value Value to use in the constructor
   * @return Constructor call
   */
  private InvokeStmt addConstructorCall(Local input, Class<?> type, Value value) {
    SootMethod newInputConstructor = InputHelper.getConstructorForType(type);
    InvokeStmt constructorInvoke = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(input, newInputConstructor.makeRef(), value));
    return constructorInvoke;
  }

}