package org.imdea.fixcheck.transform.common;

import soot.*;

import java.util.ArrayList;
import java.util.List;

/**
 * TransformationHelper class: helper class for applying transformations.
 */
public class TransformationHelper {

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
