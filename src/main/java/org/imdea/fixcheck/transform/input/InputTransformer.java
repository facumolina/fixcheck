package org.imdea.fixcheck.transform.input;

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
    SootClass prefixClass = prefix.getMethodClass();
    SootMethod prefixMethod = prefix.getMethod();
    Body oldBody = prefixMethod.retrieveActiveBody();
    // Prepare the new class
    String className = baseClassName + transformationsApplied;
    SootClass newClass = TransformationHelper.initializeTransformedClass(prefixClass.getPackageName() + "." + className, prefixClass);
    // Prepare the new method
    SootMethod newMethod = new SootMethod(basePrefixName, prefixMethod.getParameterTypes(), prefixMethod.getReturnType(), prefixMethod.getModifiers());
    newMethod.addAllTagsOf(prefixMethod);
    newMethod.setExceptions(prefixMethod.getExceptions());
    // Prepare the new body
    Body newBody = (Body)oldBody.clone();
    replaceInput(newBody);
    newMethod.setActiveBody(newBody);
    newClass.addMethod(newMethod);
    transformationsApplied++;
    return new Prefix(newMethod, newClass, prefix);
  }

  @Override
  public String getLastTransformation() {
    return lastTransformation;
  }

  /**
   * Replace a randomly selected input within the body.
   * @param body Body to transform
   */
  private void replaceInput(Body body) {
    // Get a random local
    Input input = getRandomInput(body);
    // Find the type where the input is used
    Type usageType = TransformationHelper.getTypeOfFirstUsage(input, body);
    // Determine the type of the new local based on the usage
    Class<?> type = getClassForNewInput(usageType);
    Value value = InputHelper.getValueForType(type);
    lastTransformation = "[" + input.getValue() + ":" + input.getType() + "] replaced by [" + value + ":" + type.getName()+"]";
    // Generate call for input constructor
    if (type.isPrimitive()) {
      // For primitives, just replace the old input with the new input
      TransformationHelper.replace(body, input.getValue(), value);
    } else {
      // For non-primitives, create a new local, call the constructor and then replace the old input with the new local
      Local newInput = defineLocalForType(type, body);
      AssignStmt assignStmt = Jimple.v().newAssignStmt(newInput, Jimple.v().newNewExpr(RefType.v(type.getName())));
      InvokeStmt constructorInvoke = addConstructorCall(newInput, type, value);
      // Replace old input constructor with new input constructor
      replaceConstructor(body, input, assignStmt, constructorInvoke);
      // Use the new input in the right place
      TransformationHelper.replaceIgnoring(body, input.getValue(), newInput, constructorInvoke);
    }
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
   * Get a random Local for the input class
   * @param body Body to search
   * @return Random Local for the input class
   */
  private Input getRandomInput(Body body) {
    List<Input> locals = TransformationHelper.getInputsWithType(body, Properties.INPUTS_CLASS);
    if (locals.isEmpty()) throw new IllegalArgumentException("No locals of type " + Properties.INPUTS_CLASS);
    Random random = new Random();
    int index = random.nextInt(locals.size());
    return locals.get(index);
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
   * @param type Type to get the class for
   * @return Class for the given type
   */
  private Class<?> getClassForNewInput(Type type) {
    if (InputHelper.INPUTS_BY_TYPE.containsKey(type.toString())) {
      List<Class<?>> possibleInputs = InputHelper.INPUTS_BY_TYPE.get(type.toString());
      Random random = new Random();
      int index = random.nextInt(possibleInputs.size());
      return possibleInputs.get(index);
    }
    throw new IllegalArgumentException("Type not supported: " + type);
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