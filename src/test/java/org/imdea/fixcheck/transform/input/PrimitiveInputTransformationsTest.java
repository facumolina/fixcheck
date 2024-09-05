package org.imdea.fixcheck.transform.input;

import com.github.javaparser.ast.body.MethodDeclaration;
import org.imdea.fixcheck.properties.FixCheckProperties;
import org.imdea.fixcheck.prefix.Prefix;
import org.imdea.fixcheck.transform.common.TransformationHelper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Primitive Input Transformations Test class: test the primitive input transformations.
 * @author Facundo Molina
 */
public class PrimitiveInputTransformationsTest {

  @Before
  public void initialize() {
    // Initialize stuff for inputs
    FixCheckProperties.TEST_CLASS_SRC_DIR = "src/test/java";
    FixCheckProperties.TEST_CLASS = "org.imdea.fixcheck.transform.input.PrimitiveInputTransformationsTest";
    FixCheckProperties.setup();
    InputHelper.initializeHelper();
  }

  private final InputTransformer INPUT_TRANSFORMER = new InputTransformer();

  private Prefix getTargetPrefix(String prefixMethodName) {
    // Get the corresponding method from the compilation unit
    MethodDeclaration methodDecl = TransformationHelper.getMethodDeclFromCompilationUnit(FixCheckProperties.TEST_CLASS_SRC, prefixMethodName);
    return new Prefix(methodDecl, FixCheckProperties.TEST_CLASS_SRC);
  }

  // Basic methods to be used in the tests in Target class
  private boolean basicIntMethodIsEven(int i) {
    return i % 2 == 0;
  }

  private String basicBooleanMethodToString(boolean b) {
    return b ? "true" : "false";
  }

  private boolean basicDoubleMethodIsPositive(double d) { return d > 0; }

  private class TargetClass {
    @Test
    public void testIntMethod() {
      int n = 0;
      boolean result = basicIntMethodIsEven(1);
      assertTrue(result);
    }
    @Test
    public void testBooleanMethod() {
      boolean b = true;
      String result = basicBooleanMethodToString(b);
      assertEquals("true", result);
    }
    @Test
    public void testDoubleMethod() {
      double d = 1.0;
      boolean result = basicDoubleMethodIsPositive(d);
      assertTrue(result);
    }
  }

  // Actual tests
  @Test
  public void testIntTransformation() {
    FixCheckProperties.INPUTS_CLASS = "int";
    Prefix targetPrefix = getTargetPrefix("testIntMethod");
    Prefix transformedPrefix = INPUT_TRANSFORMER.transform(targetPrefix);
    String lastTransformation = INPUT_TRANSFORMER.getLastTransformation();
    // The last transformation should match: [digit:IntegerLiteralExpr] replaced by [digit:java.lang.Integer]
    assertTrue(lastTransformation.matches("\\[\\d+:int\\] replaced by \\[\\d+:java.lang.Integer\\]"));
  }

  @Test
  public void testBooleanTransformation() {
    FixCheckProperties.INPUTS_CLASS = "boolean";
    Prefix targetPrefix = getTargetPrefix("testBooleanMethod");
    Prefix transformedPrefix = INPUT_TRANSFORMER.transform(targetPrefix);
    String lastTransformation = INPUT_TRANSFORMER.getLastTransformation();
    // The last transformation should match: [word:boolean] replaced by [word:java.lang.Boolean]
    assertTrue(lastTransformation.matches("\\[\\w+:boolean\\] replaced by \\[\\w+:java.lang.Boolean\\]"));
  }

  @Test
  public void testDoubleTransformation() {
    FixCheckProperties.INPUTS_CLASS = "double";
    Prefix targetPrefix = getTargetPrefix("testDoubleMethod");
    Prefix transformedPrefix = INPUT_TRANSFORMER.transform(targetPrefix);
    String lastTransformation = INPUT_TRANSFORMER.getLastTransformation();
    // The last transformation should match: [digit:double] replaced by [digit:java.lang.Double]
    assertTrue(lastTransformation.matches("\\[.*:double\\] replaced by \\[.*:java.lang.Double\\]"));
  }

}
