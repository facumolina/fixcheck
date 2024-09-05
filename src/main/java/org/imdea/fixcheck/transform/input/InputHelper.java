package org.imdea.fixcheck.transform.input;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.*;
import org.imdea.fixcheck.properties.FixCheckProperties;
import org.imdea.fixcheck.transform.input.provider.*;

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
    PROVIDERS.put(BooleanLiteralExpr.class, new BooleanProvider());
    PROVIDERS.put(IntegerLiteralExpr.class, new IntegerProvider());
    PROVIDERS.put(LongLiteralExpr.class, new LongProvider());
    PROVIDERS.put(DoubleLiteralExpr.class, new DoubleProvider());
    PROVIDERS.put(StringLiteralExpr.class, new StringProvider());
  }

  public static void initializeHelper() {
    // Initialize the map of inputs by type
    INPUTS_BY_TYPE = new HashMap<>();
    INPUTS_BY_TYPE.put("boolean", Collections.singletonList(BooleanLiteralExpr.class));
    INPUTS_BY_TYPE.put("java.util.Boolean", Collections.singletonList(BooleanLiteralExpr.class));
    INPUTS_BY_TYPE.put("int", Collections.singletonList(IntegerLiteralExpr.class));
    INPUTS_BY_TYPE.put("java.lang.Integer", Collections.singletonList(IntegerLiteralExpr.class));
    INPUTS_BY_TYPE.put("long", Collections.singletonList(LongLiteralExpr.class));
    INPUTS_BY_TYPE.put("java.lang.Long", Collections.singletonList(LongLiteralExpr.class));
    INPUTS_BY_TYPE.put("double", Collections.singletonList(DoubleLiteralExpr.class));
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
    CompilationUnit cu = FixCheckProperties.TEST_CLASS_SRC;
    cu.findAll(StringLiteralExpr.class).forEach(stringLiteralExpr -> PROVIDERS.get(StringLiteralExpr.class).addInput(stringLiteralExpr.getValue()));
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