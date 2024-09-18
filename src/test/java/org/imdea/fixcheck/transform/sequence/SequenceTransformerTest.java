package org.imdea.fixcheck.transform.sequence;

import com.github.javaparser.ast.body.MethodDeclaration;
import org.imdea.fixcheck.properties.FixCheckProperties;
import org.imdea.fixcheck.prefix.Prefix;
import org.imdea.fixcheck.transform.common.TransformationHelper;
import org.imdea.fixcheck.transform.input.InputHelper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * SequenceTransformerTest class: test the sequence transformations.
 * @author Facundo Molina
 */
public class SequenceTransformerTest {

  @Before
  public void initialize() {
    // Initialize stuff for inputs
    FixCheckProperties.TEST_CLASS_SRC_DIR = "src/test/java";
    FixCheckProperties.TEST_CLASS = "org.imdea.fixcheck.transform.sequence.SequenceTransformerTest";
    FixCheckProperties.setup();
    InputHelper.initializeHelper();
  }

  private final SequenceTransformer SEQ_TRANSFORMER = new SequenceTransformer();

  private Prefix getTargetPrefix(String prefixMethodName) {
    // Get the corresponding method from the compilation unit
    MethodDeclaration methodDecl = TransformationHelper.getMethodDeclFromCompilationUnit(FixCheckProperties.TEST_CLASS_SRC, prefixMethodName);
    return new Prefix(methodDecl, FixCheckProperties.TEST_CLASS_SRC);
  }

  // Basic methods to be used in the tests in Target class
  private int one() { return 1;}
  private int two() { return 2;}

  private class TargetClass {
    @Test
    public void simpleMethod() {
      int i = one();
      int j = two();
      i += 1; // Target removable sequence
      int k = i + j;
      assertEquals(4, k);
    }
  }

  // Actual tests
  @Test
  public void testRemoveSequence() {
    // Get the corresponding method from the compilation unit
    FixCheckProperties.INPUTS_CLASS = "int";
    Prefix prefix = getTargetPrefix("simpleMethod");

    // Transform the prefix
    Prefix transformedPrefix = SEQ_TRANSFORMER.transform(prefix);

    // Check the transformation
    //assertNotNull(transformedPrefix);
  }
}
