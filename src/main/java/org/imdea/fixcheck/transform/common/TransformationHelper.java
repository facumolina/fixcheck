package org.imdea.fixcheck.transform.common;

import org.imdea.fixcheck.prefix.ConstantInput;
import org.imdea.fixcheck.prefix.Input;
import org.imdea.fixcheck.prefix.LocalInput;
import soot.*;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.internal.JInvokeStmt;

import java.util.ArrayList;
import java.util.List;

/**
 * TransformationHelper class: helper class for applying transformations.
 */
public class TransformationHelper {

  /**
   * Initialize a transformed class from a soot class.
   * @param newClassName is the name of the new class.
   * @param sootClass is the soot class to initialize the new class from.
   * @return the new transformed class.
   */
  public static SootClass initializeTransformedClass(String newClassName, SootClass sootClass) {
    SootClass transformedClass = new SootClass(newClassName, sootClass.getModifiers(), sootClass.moduleName);
    transformedClass.setSuperclass(sootClass.getSuperclass());
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
   * Return the Type of the first usage of a given local in a body.
   * The type is essentially the type of the parameter of the method call that uses the local.
   * @param input Input to search
   * @param body Body to search
   * @return Type of the first usage of the given local
   */
  public static Type getTypeOfFirstUsage(Input input, Body body) {
    Unit unit = getFirstUnitUsingInput(input, body);
    JInvokeStmt stmt = ((JInvokeStmt) unit);
    return stmt.getInvokeExpr().getMethod().getParameterType(getIndexForValue(stmt, input.getValue()));
  }

  /**
   * Get first Unit using the input
   * @param input Input to search
   * @param body Body to search
   * @return First unit using the input
   */
  public static Unit getFirstUnitUsingInput(Input input, Body body) {
    for (Unit ut : body.getUnits()) {
      for (ValueBox vb : ut.getUseBoxes()) {
        if (vb.getValue().equals(input.getValue())) {
          if (ut instanceof JInvokeStmt) {
            JInvokeStmt stmt = ((JInvokeStmt) ut);
            if (isConstructorCall(stmt, input.getValue())) continue;
            return ut;
          }
        }
      }
    }
    throw new IllegalArgumentException("Unable to find unit using Input " + input + ". Is it used?");
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
          if (ut instanceof JInvokeStmt && isConstructorCall((JInvokeStmt)ut,local))
            continue;
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
   * Return the list of inputs of a given type in a body
   * @param body Body to search
   * @param typeName Type of the local to search
   * @return List of Inputs of the given type, empty if not found
   */
  public static List<Input> getInputsWithType(Body body, String typeName) {
    List<Input> inputs = new ArrayList<>();
    // First, search for locals
    for (Local local : body.getLocals()) {
      if (local.getType().toString().equals(typeName)) {
        inputs.add(new LocalInput(typeName, local));
      }
    }
    // Second, search for constants if applicable
    if (canBeConstant(typeName)) {
      List<ValueBox> useBoxes = body.getUseBoxes();
      for (ValueBox vb : useBoxes) {
        if (vb.getValue().getType().toString().equals(typeName)) {
          inputs.add(new ConstantInput(typeName, vb.getValue()));
        }
      }
    }
    return inputs;
  }

  public static boolean canBeConstant(String typeName) {
    return typeName.equals("java.lang.String");
  }

  /**
   * Replace all uses of v1 in body with v2
   * @param body Body to modify
   * @param v1 Value to replace
   * @param v2 Value to replace with
   */
  public static void replace(Body body, Value v1, Value v2) {
    for (Unit ut : body.getUnits()) {
      for (ValueBox vb : ut.getUseBoxes())
        if( vb.getValue().equals(v1))
          vb.setValue(v2);
    }
  }

}
