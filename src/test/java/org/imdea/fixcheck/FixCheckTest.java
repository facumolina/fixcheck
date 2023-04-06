package org.imdea.fixcheck;

import org.imdea.fixcheck.prefix.Prefix;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FixCheckTest {

  @Test
  public void testJacksonDatabind118() throws ClassNotFoundException, IOException {
    // Set the properties
    String fixCheckPath = "build/libs/fixcheck-all-1.0.0.jar";
    String projectCp = "/Users/facundo.molina/research/software/bad-fixes-dataset/tmp/jackson-databind-118/badfix/jackson-databind/target/jackson-databind-2.1.2-SNAPSHOT-jar-with-dependencies.jar";
    String projectTestsCp = "/Users/facundo.molina/research/software/bad-fixes-dataset/tmp/jackson-databind-118/badfix/jackson-databind/target/test-classes";
    Properties.FULL_CLASSPATH = fixCheckPath + ":" + projectCp + ":" + projectTestsCp;
    Properties.TEST_CLASSES_PATH = projectTestsCp;
    Properties.TEST_CLASS = "com.fasterxml.jackson.databind.jsontype.TestExternalIdForAnalysis";
    Properties.TEST_CLASS_METHODS = new String[]{"testWithScalar118"};
    Properties.TEST_CLASS_SRC_DIR = "/Users/facundo.molina/research/software/bad-fixes-dataset/tmp/jackson-databind-118/badfix/jackson-databind/src/test/java";
    Properties.TARGET_CLASS = "com.fasterxml.jackson.databind.ObjectMapper";
    Properties.INPUTS_CLASS = "java.util.Date";
    // Load the prefixes to analyze
    List<Prefix> prefixes = Properties.getPrefixes();
    // Generate similar prefixes
    List<Prefix> similarPrefixes = FixCheck.generateSimilarPrefixes(prefixes, 1);
    assertEquals(1, similarPrefixes.size());
  }

  @Test
  public void testGraphhopper172() throws ClassNotFoundException, IOException {
    // Set the properties
    String fixCheckPath = "build/libs/fixcheck-all-1.0.0.jar";
    String projectCp = "/Users/facundo.molina/research/software/bad-fixes-dataset/tmp/graphhopper-172/badfix/graphhopper/core/target/graphhopper-0.3-SNAPSHOT-jar-with-dependencies.jar";
    String projectTestsCp = "/Users/facundo.molina/research/software/bad-fixes-dataset/tmp/graphhopper-172/badfix/graphhopper/core/target/test-classes";
    Properties.FULL_CLASSPATH = fixCheckPath + ":" + projectCp + ":" + projectTestsCp;
    Properties.TEST_CLASSES_PATH = projectTestsCp;
    Properties.TEST_CLASS = "com.graphhopper.routing.util.CarFlagEncoderTestForAnalysis";
    Properties.TEST_CLASS_METHODS = new String[]{"testMaxSpeed"};
    Properties.TEST_CLASS_SRC_DIR = "/Users/facundo.molina/research/software/bad-fixes-dataset/tmp/graphhopper-172/badfix/graphhopper/core/src/test/java";
    Properties.TARGET_CLASS = "com.graphhopper.routing.util.CarFlagEncoder";
    Properties.INPUTS_CLASS = "java.lang.String";
    // Load the prefixes to analyze
    List<Prefix> prefixes = Properties.getPrefixes();
    // Generate similar prefixes
    List<Prefix> similarPrefixes = FixCheck.generateSimilarPrefixes(prefixes, 2);
    assertEquals(2, similarPrefixes.size());
  }

  @Test
  public void testChoco600() throws ClassNotFoundException, IOException {
    // Set the properties
    String fixCheckPath = "build/libs/fixcheck-all-1.0.0.jar";
    String projectCp = "/Users/facundo.molina/research/software/bad-fixes-dataset/tmp/choco-600/badfix/choco-solver/target/choco-solver-4.0.9-SNAPSHOT.jar";
    String projectTestsCp = "/Users/facundo.molina/research/software/bad-fixes-dataset/tmp/choco-600/badfix/choco-solver/target/test-classes";
    Properties.FULL_CLASSPATH = fixCheckPath + ":" + projectCp + ":" + projectTestsCp;
    Properties.TEST_CLASSES_PATH = projectTestsCp;
    Properties.TEST_CLASS = "org.chocosolver.solver.constraints.binary.ElementTestForAnalysis";
    Properties.TEST_CLASS_METHODS = new String[]{"improveElement1"};
    Properties.TEST_CLASS_SRC_DIR = "/Users/facundo.molina/research/software/bad-fixes-dataset/tmp/choco-600/badfix/choco-solver/src/test/java";
    Properties.TARGET_CLASS = "org.chocosolver.solver.Model";
    Properties.INPUTS_CLASS = "int";
    // Load the prefixes to analyze
    List<Prefix> prefixes = Properties.getPrefixes();
    // Generate similar prefixes
    List<Prefix> similarPrefixes = FixCheck.generateSimilarPrefixes(prefixes, 1);
    assertEquals(1, similarPrefixes.size());
  }

}