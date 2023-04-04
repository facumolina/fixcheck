package org.imdea.fixcheck.transform.common;

import org.imdea.fixcheck.prefix.ConstantInput;
import org.imdea.fixcheck.prefix.Input;
import org.imdea.fixcheck.prefix.LocalInput;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;

import java.util.ArrayList;
import java.util.List;

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
   * Return the Type of the first usage of a given input in a body.
   * If the input is a LocalInput, then the type is essentially the type of the parameter
   * of the method call that uses the local.
   * If the input is a ConstantInput, then the type is the type of the constant.
   * @param input Input to search
   * @param body Body to search
   * @return Type of the first usage of the given local
   */
  public static Type getTypeOfFirstUsage(Input input, Body body) {
    if (input instanceof LocalInput) {
      Unit unit = getFirstUnitUsingInput(input, body);
      if (unit instanceof JInvokeStmt) {
        JInvokeStmt stmt = ((JInvokeStmt) unit);
        int index = getIndexForValue(stmt, input.getValue());
        return stmt.getInvokeExpr().getMethod().getParameterType(index);
      } else {
        return input.getValue().getType();
      }
    } else if (input instanceof ConstantInput) {
      return input.getValue().getType();
    }
    throw new IllegalArgumentException("Input type not supported: "+input.getClass().getName());
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
            // Avoid the constructions of the given local
            JInvokeStmt stmt = ((JInvokeStmt) ut);
            if (isConstructorCall(stmt, input.getValue())) continue;
            return ut;
          } else if (ut instanceof JAssignStmt) {
            // Avoid the definition of the given local
            JAssignStmt stmt = ((JAssignStmt) ut);
            if (stmt.getLeftOp().equals(input.getValue())) continue;
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
        if (isLocalUsed(local, body))
          inputs.add(new LocalInput(typeName, local));
      }
    }
    // Second, search for constants if applicable
    if (canBeConstant(typeName)) {
      List<ValueBox> useBoxes = body.getUseBoxes();
      for (ValueBox vb : useBoxes) {
        if (vb.getValue().getType().toString().equals(typeName)) {
          List<Unit> usingConstant = getUnitsUsingConstant(vb.getValue(), body);
          if (usingConstant.isEmpty()) continue;
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
