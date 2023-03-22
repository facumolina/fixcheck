package org.imdea.fixcheck.transform.input;

import soot.Scene;
import soot.SootMethod;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.StringConstant;

import java.util.Random;

/**
 * Input Helper class: provides methods to handle the creation of inputs of specific types.
 * @author Facundo Molina
 */
public class InputHelper {

  /**
   * Get the constructor method for the given type.
   * @param type Type of the input
   * @return Constructor method
   */
  public static SootMethod getConstructorForType(Class<?> type) {
    if (type.equals(Integer.class))
      return Scene.v().getMethod("<java.lang.Integer: void <init>(int)>");
    if (type.equals(String.class))
      return Scene.v().getMethod("<java.lang.String: void <init>(java.lang.String)>");
    throw new IllegalArgumentException("Type not supported: " + type.toString());
  }

  /**
   * Get a value for the given type.
   * @param type Type of the input
   * @return Value for the given type
   */
  public static Value getValueForType(Class<?> type) {
    if (type.equals(Integer.class)) {
      Random random = new Random();
      return IntConstant.v(random.nextInt(100));
    }
    if (type.equals(String.class))
      return StringConstant.v("test");
    throw new IllegalArgumentException("Type not supported: " + type);
  }

}