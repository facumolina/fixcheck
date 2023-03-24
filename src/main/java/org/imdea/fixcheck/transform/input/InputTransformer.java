package org.imdea.fixcheck.transform.input;

import org.imdea.fixcheck.Properties;
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

  public InputTransformer() { }

  @Override
  public Prefix transform(Prefix prefix) {
    SootClass prefixClass = prefix.getMethodClass();
    SootMethod prefixMethod = prefix.getMethod();
    Body oldBody = prefixMethod.retrieveActiveBody();

    SootClass newClass = TransformationHelper.initializeTransformedClass(prefixClass.getPackageName() + ".SimilarPrefixClass", prefixClass);
    SootMethod newMethod = new SootMethod("similarPrefix", prefixMethod.getParameterTypes(), prefixMethod.getReturnType(), prefixMethod.getModifiers());
    newMethod.addAllTagsOf(prefixMethod);
    newMethod.setExceptions(prefixMethod.getExceptions());
    Body newBody = (Body)oldBody.clone();
    replaceInput(newBody);
    newMethod.setActiveBody(newBody);
    newClass.addMethod(newMethod);

    return new Prefix(newMethod, newClass);
  }

  private void replaceInput(Body body) {
    // Get a random local
    Local input = getRandomLocal(body);
    // Find the type where the input is used
    Type usageType = TransformationHelper.getTypeOfFirstUsage(input, body);
    // Determine the type of the new local based on the usage
    Class<?> type = getClassForNewInput(usageType);
    Local newInput = defineLocalForType(type, body);
    // Generate call for input constructor
    AssignStmt assignStmt = Jimple.v().newAssignStmt(newInput, Jimple.v().newNewExpr(RefType.v(type.getName())));
    InvokeStmt constructorInvoke = addConstructorCall(newInput, type);
    // Replace old input constructor with new input constructor
    replaceConstructor(body, input, assignStmt, constructorInvoke);
    // Use the new input in the right place
    TransformationHelper.replace(body, input, newInput);
  }

  private void replaceConstructor(Body body, Local inputToReplace, AssignStmt assignStmt, InvokeStmt constructorInvoke) {
    // First replace the assignment
    for (Unit ut : body.getUnits()) {
      if (ut instanceof AssignStmt) {
        AssignStmt assign = (AssignStmt) ut;
        if (assign.getLeftOp().equals(inputToReplace)) {
          body.getUnits().swapWith(ut, assignStmt);
          break;
        }
      }
    }
    // Then replace the actual constructor call
    for (Unit ut : body.getUnits()) {
      if (ut instanceof InvokeStmt) {
        InvokeExpr invokeExpr = ((InvokeStmt)ut).getInvokeExpr();
        boolean classMatch = invokeExpr.getMethod().getDeclaringClass().getName().equals(inputToReplace.getType().toString());
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
  private Local getRandomLocal(Body body) {
    List<Local> locals = TransformationHelper.getLocalsWithType(body, Properties.INPUTS_CLASS);
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
   * @return Constructor call
   */
  private InvokeStmt addConstructorCall(Local input, Class<?> type) {
    SootMethod newInputConstructor = InputHelper.getConstructorForType(type);
    Value value = InputHelper.getValueForType(type);
    InvokeStmt constructorInvoke = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(input, newInputConstructor.makeRef(), value));
    return constructorInvoke;
  }

}