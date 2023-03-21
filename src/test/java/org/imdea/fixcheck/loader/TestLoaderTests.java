package org.imdea.fixcheck.loader;

import org.imdea.fixcheck.Properties;
import org.imdea.fixcheck.prefix.Prefix;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * TestLoaderTests class: tests for TestLoader.
 *
 * @author Facundo Molina <facundo.molina@imdea.org>
 */
public class TestLoaderTests {

  @Test
  public void testLoadPrefixes() {
    // Set the properties
    String fixCheckPath = "build/libs/fixcheck-all-1.0.0.jar";
    String projectCp = "/Users/facundo.molina/research/software/bad-fixes-dataset/tmp/jackson-databind-118/badfix/jackson-databind/target/jackson-databind-2.1.2-SNAPSHOT-jar-with-dependencies.jar";
    String projectTestsCp = "/Users/facundo.molina/research/software/bad-fixes-dataset/tmp/jackson-databind-118/badfix/jackson-databind/target/test-classes";
    org.imdea.fixcheck.Properties.TEST_CLASSES_PATH = fixCheckPath + ":" + projectCp + ":" + projectTestsCp;
    org.imdea.fixcheck.Properties.TEST_CLASS = "com.fasterxml.jackson.databind.jsontype.TestExternalIdForAnalysis";
    Properties.TARGET_CLASS = "com.fasterxml.jackson.databind.ObjectMapper";
    List<Prefix> prefixes = TestLoader.loadPrefixes();
    assertNotNull(prefixes);
    assertEquals(1, prefixes.size());
  }

}