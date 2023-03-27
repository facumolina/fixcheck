package org.imdea.fixcheck.transform.input;

import org.imdea.fixcheck.transform.input.provider.*;
import soot.Scene;
import soot.SootMethod;
import soot.Value;

import java.util.*;

/**
 * Input Helper class: provides methods to handle the creation of inputs of specific types.
 * @author Facundo Molina
 */
public class InputHelper {

  protected static Map<String, List<Class<?>>> INPUTS_BY_TYPE; // Map of possible input classes for each type
  private static Map<Class<?>, InputProvider> providers;
  static {
    providers = new HashMap<>();
    providers.put(java.lang.Integer.class, new IntegerProvider());
    providers.put(java.lang.Boolean.class, new BooleanProvider());
    providers.put(java.lang.Long.class, new LongProvider());
    providers.put(java.lang.String.class, new StringProvider());
  }

  public static void initializeInputsByType() {
    INPUTS_BY_TYPE = new HashMap<>();
    INPUTS_BY_TYPE.put("java.util.Boolean", Collections.singletonList(Boolean.class));
    INPUTS_BY_TYPE.put("java.lang.Integer", Collections.singletonList(Integer.class));
    INPUTS_BY_TYPE.put("java.lang.Long", Collections.singletonList(Long.class));
    INPUTS_BY_TYPE.put("java.lang.Float", Collections.singletonList(Float.class));
    INPUTS_BY_TYPE.put("java.lang.Double", Collections.singletonList(Double.class));
    INPUTS_BY_TYPE.put("java.lang.String", Collections.singletonList(String.class));
    INPUTS_BY_TYPE.put("java.lang.Object", Arrays.asList(Boolean.class, Integer.class, Long.class, String.class));
  }

  /**
   * Get the constructor method for the given type.
   * @param type Type of the input
   * @return Constructor method
   */
  public static SootMethod getConstructorForType(Class<?> type) {
    if (type.equals(Boolean.class))
      return Scene.v().getMethod("<java.lang.Boolean: void <init>(boolean)>");
    if (type.equals(Integer.class))
      return Scene.v().getMethod("<java.lang.Integer: void <init>(int)>");
    if (type.equals(Long.class))
      return Scene.v().getMethod("<java.lang.Long: void <init>(long)>");
    if (type.equals(String.class))
      return Scene.v().getMethod("<java.lang.String: void <init>(java.lang.String)>");
    throw new IllegalArgumentException("Type not supported: " + type);
  }

  /**
   * Get a value for the given type.
   * @param type Type of the input
   * @return Value for the given type
   */
  public static Value getValueForType(Class<?> type) {
    if (providers.containsKey(type)) {
      InputProvider provider = providers.get(type);
      return provider.getInput();
    }
    throw new IllegalArgumentException("Type not supported, don't know how to get values for : " + type);
  }

}