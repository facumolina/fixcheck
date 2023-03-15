package org.imdea.fixcheck.loader;

import org.junit.Test;
import sootup.core.model.SootClass;
import sootup.java.core.JavaSootClassSource;

import static org.junit.Assert.assertNotNull;

/**
 * TestLoaderTests class: tests for TestLoader.
 *
 * @author Facundo Molina <facundo.molina@imdea.org>
 */
public class TestLoaderTests {

  @Test
  public void testLoadTestClass() {
    String testClass = "com.fasterxml.jackson.databind.jsontype.TestExternalIdForAnalysis";
    TestLoader.setup("/Users/facundo.molina/research/software/bad-fixes-dataset/tmp/jackson-databind-118/badfix/jackson-databind/target/test-classes");
    SootClass<JavaSootClassSource> sootClass = TestLoader.loadTestClass(testClass);
    assertNotNull(sootClass);
  }

}