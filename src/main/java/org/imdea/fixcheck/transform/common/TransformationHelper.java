package org.imdea.fixcheck.transform.common;

import soot.*;
import soot.jimple.DefinitionStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.internal.JInvokeStmt;

import java.util.ArrayList;
import java.util.List;

/**
 * TransformationHelper class: helper class for applying transformations.
 */
public class TransformationHelper {

  /**
   * Return the first unit that uses a given local in the given body
   * @param local Local to search
   * @param body Body to search
   * @return First unit that uses the given local, null if not found
   */
  public static Unit getFirstUnitUsingLocal(Local local, Body body) {
    for (Unit ut : body.getUnits()) {
      for (ValueBox vb : ut.getUseBoxes()) {
        if (vb.getValue().equals(local)) {
          if (ut instanceof JInvokeStmt && isConstructorCall((JInvokeStmt)ut,local))
            continue;
          return ut;
        }
      }
    }
    return null;
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
   * @param local Local to check
   * @return True if the invoke statement is a constructor call
   */
  public static boolean isConstructorCall(JInvokeStmt invokeStmt, Local local) {
    if (invokeStmt.getInvokeExpr() instanceof SpecialInvokeExpr) {
      SpecialInvokeExpr specialInvokeExpr = (SpecialInvokeExpr) invokeStmt.getInvokeExpr();
      if (specialInvokeExpr.getBase().equals(local) && specialInvokeExpr.getMethod().getName().equals("<init>"))
        return true;
    }
    return false;
  }

  /**
   * Return the list of locals of a given type in a body
   * @param body Body to search
   * @param typeName Type of the local to search
   * @return List of Locals of the given type, empty if not found
   */
  public static List<Local> getLocalsWithType(Body body, String typeName) {
    List<Local> locals = new ArrayList<>();
    for (Local local : body.getLocals()) {
      if (local.getType().toString().equals(typeName)) {
        locals.add(local);
      }
    }
    return locals;
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
