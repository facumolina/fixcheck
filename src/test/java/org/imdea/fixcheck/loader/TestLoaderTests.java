package org.imdea.fixcheck.loader;

import org.imdea.fixcheck.prefix.Prefix;
import org.junit.Test;
import sootup.core.model.SootClass;
import sootup.java.core.JavaSootClassSource;

import java.util.List;

import static org.junit.Assert.*;

/**
 * TestLoaderTests class: tests for TestLoader.
 *
 * @author Facundo Molina <facundo.molina@imdea.org>
 */
public class TestLoaderTests {

  @Test
  public void testLoadTestClass() {
    String classesPath = "/Users/facundo.molina/research/software/bad-fixes-dataset/tmp/jackson-databind-118/badfix/jackson-databind/target/test-classes";
    String testClass = "com.fasterxml.jackson.databind.jsontype.TestExternalIdForAnalysis";
    TestLoader.setup(classesPath);
    SootClass<JavaSootClassSource> sootClass = TestLoader.loadTestClass(testClass);
    assertNotNull(sootClass);
  }

  @Test
  public void testLoadPrefixes() {
    String classesPath = "/Users/facundo.molina/research/software/bad-fixes-dataset/tmp/jackson-databind-118/badfix/jackson-databind/target/test-classes";
    String testClass = "com.fasterxml.jackson.databind.jsontype.TestExternalIdForAnalysis";
    TestLoader.setup(classesPath);
    List<Prefix> prefixes = TestLoader.loadPrefixes(classesPath, testClass);
    assertNotNull(prefixes);
    assertEquals(1, prefixes.size());
  }

}