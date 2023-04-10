package org.imdea.fixcheck.transform.input;

import com.github.javaparser.ast.expr.*;
import org.imdea.fixcheck.transform.input.provider.*;
import soot.*;

import java.util.*;

/**
 * Input Helper class: provides methods to handle the creation of inputs of specific types.
 * @author Facundo Molina
 */
public class InputHelper {

  protected static Map<String, List<Class<? extends Expression>>> INPUTS_BY_TYPE; // Map of possible input classes for each type
  public static Map<Class<? extends Expression>, InputProvider> PROVIDERS;
  static {
    PROVIDERS = new HashMap<>();
    PROVIDERS.put(IntegerLiteralExpr.class, new IntegerProvider());
    PROVIDERS.put(BooleanLiteralExpr.class, new BooleanProvider());
    PROVIDERS.put(LongLiteralExpr.class, new LongProvider());
    PROVIDERS.put(StringLiteralExpr.class, new StringProvider());
  }

  public static void initializeHelper() {
    // Initialize the map of inputs by type
    INPUTS_BY_TYPE = new HashMap<>();
    INPUTS_BY_TYPE.put("java.util.Boolean", Collections.singletonList(BooleanLiteralExpr.class));
    INPUTS_BY_TYPE.put("int", Collections.singletonList(IntegerLiteralExpr.class));
    INPUTS_BY_TYPE.put("java.lang.Integer", Collections.singletonList(IntegerLiteralExpr.class));
    INPUTS_BY_TYPE.put("java.lang.Long", Collections.singletonList(LongLiteralExpr.class));
    //INPUTS_BY_TYPE.put("java.lang.Float", Collections.singletonList(FloatLi));
    INPUTS_BY_TYPE.put("java.lang.Double", Collections.singletonList(DoubleLiteralExpr.class));
    INPUTS_BY_TYPE.put("java.lang.String", Collections.singletonList(StringLiteralExpr.class));
    INPUTS_BY_TYPE.put("Object", Arrays.asList(BooleanLiteralExpr.class, IntegerLiteralExpr.class, LongLiteralExpr.class, StringLiteralExpr.class));
    // Feed providers with inputs that can be obtained from the test suite under analysis
    searchForInputs();
  }

  /**
   * Return true if the given class is a known input class.
   */
  public static boolean isKnownClass(String className) {
    return INPUTS_BY_TYPE.containsKey(className);
  }

  /**
   * Search for inputs in the test suite under analysis.
   */
  private static void searchForInputs() {
    /*SootClass sootTestClass = Properties.SOOT_TEST_CLASS;
    // Search for constant inputs used in the methods of the soot test class
    for (SootMethod method : sootTestClass.getMethods()) {
      // We don't want to process init methods
      if (method.getName().equals("<init>")) continue;
      for (ValueBox valueBox : method.retrieveActiveBody().getUseBoxes()) {
        Value value = valueBox.getValue();
        if (!(value instanceof Constant)) continue;
        // Print value and type
        if (value.getType().toString().equals("java.lang.Integer") || value.getType().toString().equals("int")) {
          PROVIDERS.get(Integer.class).addInput(value);
        } else if (value.getType().toString().equals("java.lang.Long") || value.getType().toString().equals("long")) {
          PROVIDERS.get(Long.class).addInput(value);
        } else if (value.getType().toString().equals("java.lang.String")) {
          PROVIDERS.get(String.class).addInput(value);
        } else if (value.getType().toString().equals("java.lang.Boolean") || value.getType().toString().equals("boolean")) {
          PROVIDERS.get(Boolean.class).addInput(value);
        }
      }
    }*/
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
  public static Object getValueForType(Class<? extends Expression> type) {
    if (PROVIDERS.containsKey(type)) {
      InputProvider provider = PROVIDERS.get(type);
      return provider.getInput();
    }
    throw new IllegalArgumentException("Type not supported, don't know how to get values for : " + type);
  }

}