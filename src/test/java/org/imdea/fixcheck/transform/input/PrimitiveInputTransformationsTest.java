package org.imdea.fixcheck.transform.input;

import org.imdea.fixcheck.Properties;
import org.imdea.fixcheck.prefix.Prefix;
import org.junit.Before;
import org.junit.Test;
import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.jimple.IntConstant;
import soot.options.Options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Primitive Input Transformations Test class: test the primitive input transformations.
 * @author Facundo Molina
 */
public class PrimitiveInputTransformationsTest {

  @Before
  public void initialize() {
    // Setup Soot
    G.reset();
    Options.v().set_prepend_classpath(true);
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_soot_classpath(System.getProperty("java.class.path"));
    // Load the test class
    SootClass sc = Scene.v().loadClassAndSupport(TargetClass.class.getName());
    sc.setApplicationClass();
    Scene.v().loadNecessaryClasses();
    Properties.SOOT_TEST_CLASS = sc;
    // Initialize stuff for inputs
    InputHelper.initializeHelper();
  }

  private final InputTransformer INPUT_TRANSFORMER = new InputTransformer();

  private Prefix getTargetPrefix(String prefixMethodName) {
    // Get the corresponding prefix
    return new Prefix(Properties.SOOT_TEST_CLASS.getMethodByName(prefixMethodName), Properties.SOOT_TEST_CLASS);
  }

  // Basic methods to be used in the tests in Target class
  private boolean basicIntMethodIsEven(int i) {
    return i % 2 == 0;
  }

  private String basicBooleanMethodToString(boolean b) {
    return b ? "true" : "false";
  }

  private class TargetClass {
    @Test
    public void testIntMethod() {
      int n = 0;
      boolean result = basicIntMethodIsEven(n);
      assertTrue(result);
    }
    @Test
    public void testBooleanMethod() {
      boolean b = true;
      String result = basicBooleanMethodToString(b);
      assertEquals("true", result);
    }
  }

  @Test
  public void testIntTransformation() {
    Properties.INPUTS_CLASS = "int";
    Prefix targetPrefix = getTargetPrefix("testIntMethod");
    Prefix transformedPrefix = INPUT_TRANSFORMER.transform(targetPrefix);
    String lastTransformation = INPUT_TRANSFORMER.getLastTransformation();
    // The last transformation should match: [digit:int] replaced by [digit:int]
    assertTrue(lastTransformation.matches("\\[\\d+:int\\] replaced by \\[\\d+:int\\]"));
    // Value in prefix should have changed
    IntConstant oldValue = (IntConstant) targetPrefix.getMethod().getActiveBody().getUseBoxes().get(3).getValue().getUseBoxes().get(1).getValue();
    IntConstant newValue = (IntConstant) transformedPrefix.getMethod().getActiveBody().getUseBoxes().get(3).getValue().getUseBoxes().get(1).getValue();
    assertTrue(oldValue.value != newValue.value);
  }

  @Test
  public void testBooleanTransformation() {
    Properties.INPUTS_CLASS = "boolean";
    Prefix targetPrefix = getTargetPrefix("testBooleanMethod");
    Prefix transformedPrefix = INPUT_TRANSFORMER.transform(targetPrefix);
    String lastTransformation = INPUT_TRANSFORMER.getLastTransformation();
    // The last transformation should match: [word:int] replaced by [0:boolean]
    assertTrue(lastTransformation.matches("\\[\\w+:int\\] replaced by \\[\\d+:boolean\\]"));
  }

}
