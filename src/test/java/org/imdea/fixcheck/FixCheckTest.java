package org.imdea.fixcheck;

import org.imdea.fixcheck.loader.TestLoader;
import org.imdea.fixcheck.prefix.Prefix;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FixCheckTest {

  @Test
  public void testSimilarPrefixGeneration() throws ClassNotFoundException, IOException {
    // Set the properties
    String fixCheckPath = "build/libs/fixcheck-all-1.0.0.jar";
    String projectCp = "/Users/facundo.molina/research/software/bad-fixes-dataset/tmp/jackson-databind-118/badfix/jackson-databind/target/jackson-databind-2.1.2-SNAPSHOT-jar-with-dependencies.jar";
    String projectTestsCp = "/Users/facundo.molina/research/software/bad-fixes-dataset/tmp/jackson-databind-118/badfix/jackson-databind/target/test-classes";
    Properties.TEST_CLASSES_PATH = fixCheckPath + ":" + projectCp + ":" + projectTestsCp;
    Properties.TEST_CLASS = "com.fasterxml.jackson.databind.jsontype.TestExternalIdForAnalysis";
    Properties.TARGET_CLASS = "com.fasterxml.jackson.databind.ObjectMapper";
    // Load the prefixes to analyze
    List<Prefix> prefixes = Properties.getPrefixes();
    // Generate similar prefixes
    List<Prefix> similarPrefixes = FixCheck.generateSimilarPrefixes(prefixes, 1);
    assertEquals(1, similarPrefixes.size());
  }

}