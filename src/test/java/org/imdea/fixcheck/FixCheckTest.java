package org.imdea.fixcheck;

import org.imdea.fixcheck.loader.TestLoader;
import org.imdea.fixcheck.prefix.Prefix;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class FixCheckTest {

  @Test
  public void testSimilarPrefixGeneration() throws ClassNotFoundException {
    // Load the prefixes to analyze
    String classesPath = "/Users/facundo.molina/research/software/bad-fixes-dataset/tmp/jackson-databind-118/badfix/jackson-databind/target/test-classes";
    String testClass = "com.fasterxml.jackson.databind.jsontype.TestExternalIdForAnalysis";
    List<Prefix> prefixes = TestLoader.loadPrefixes(classesPath, testClass);
    // Generate similar prefixes
    List<Prefix> similarPrefixes = FixCheck.generateSimilarPrefixes(prefixes, 10);
    assertEquals(0, similarPrefixes.size());
  }

}