package org.imdea.fixcheck.properties;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.utils.SourceRoot;
import org.imdea.fixcheck.prefix.Prefix;
import org.imdea.fixcheck.transform.input.InputHelper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Properties class: contains properties to configure the FixCheck execution.
 * @author Facundo Molina
 */
public class FixCheckProperties {

  // General properties
  public static String FULL_CLASSPATH = System.getProperty("java.class.path"); // Full classpath (test classes + dependencies + fixcheck)

  // Properties related to the tests
  public static String TEST_CLASSES_PATH; // Path to where the test classes are located
  public static String TEST_CLASS_SRC_DIR; // Source file dir for tests
  public static String TEST_CLASS; // Full name of the test class
  public static String TEST_CLASS_SIMPLE_NAME; // Simple name of the test class
  public static String TEST_CLASS_PACKAGE_NAME; // Package name of the test class
  public static CompilationUnit TEST_CLASS_SRC; // Source file of test class
  public static String[] TEST_CLASS_METHODS; // Methods in the test class to analyze
  public static int PREFIXES_IN_TEST_CLASS = 0; // Number of prefixes in the test class
  public static String ORIGINAL_FAILURE_LOG; // Log file with the original failure
  public static String ORIGINAL_FAILURE_STR; // String wht the original failure file content

  // Properties related to the target class
  public static String TARGET_CLASS; // Full name of the target class

  // Properties related to prefixes
  public static int PREFIX_VARIATIONS = 1; // Total number of prefix variations to generate
  public static String ASSERTION_GENERATOR = "org.imdea.fixcheck.assertion.AssertTrueGenerator"; // Assertion mechanism to use

  // Properties related to inputs
  public static String INPUTS_CLASS; // Full name of the inputs class

  // Output files
  public static String OUTPUT_DIR = "fixcheck-output"; // Output directory
  public static String OUTPUT_REPORT = "report.csv"; // Output file for stats
  public static String OUTPUT_PASSING_PREFIXES_DIR = "passing-tests"; // Output dir for passing tests
  public static String OUTPUT_FAILING_PREFIXES_DIR = "failing-tests"; // Output dir for failing tests
  public static String OUTPUT_NONCOMPILING_PREFIXES_DIR = "non-compiling-tests"; // Output dir for non compiling tests
  public static String OUTPUT_SCORES = "scores-failing-tests.csv"; // Output file for score of each failing prefix

  /**
   * Load properties from the given properties file.
   */
  public static void loadProperties(String propertiesFile) {
    try (InputStream input = Files.newInputStream(Paths.get(propertiesFile))) {

      Properties prop = new Properties();

      // load a properties file
      prop.load(input);

      // get the property value and print it out
      FixCheckProperties.TEST_CLASSES_PATH = prop.getProperty("test-classes-path");
      if (FixCheckProperties.TEST_CLASSES_PATH == null) {
        System.out.println("Error: test-classes-path property is required");
        System.exit(1);
      }
      FixCheckProperties.TEST_CLASS = prop.getProperty("test-class");
      if (FixCheckProperties.TEST_CLASS == null) {
        System.out.println("Error: test-class property is required");
        System.exit(1);
      }
      if (prop.getProperty("test-methods") == null) {
        System.out.println("Error: test-methods property is required");
        System.exit(1);
      }
      FixCheckProperties.TEST_CLASS_METHODS = prop.getProperty("test-methods").split(":");
      FixCheckProperties.TEST_CLASS_SRC_DIR = prop.getProperty("test-classes-src");
      if (FixCheckProperties.TEST_CLASS_SRC_DIR == null) {
        System.out.println("Error: test-classes-src property is required");
        System.exit(1);
      }
      FixCheckProperties.ORIGINAL_FAILURE_LOG = prop.getProperty("test-failure-trace-log");
      if (FixCheckProperties.ORIGINAL_FAILURE_LOG == null) {
        System.out.println("Error: test-failure-trace-log property is required");
        System.exit(1);
      }
      FixCheckProperties.TARGET_CLASS = "";
      FixCheckProperties.INPUTS_CLASS = prop.getProperty("inputs-class");
      if (FixCheckProperties.INPUTS_CLASS == null) {
        System.out.println("Error: inputs-class property is required");
        System.exit(1);
      }
      if (prop.getProperty("number-of-prefixes") == null) {
        System.out.println("Error: number-of-prefixes property is required");
        System.exit(1);
      }
      FixCheckProperties.PREFIX_VARIATIONS = Integer.parseInt(prop.getProperty("number-of-prefixes"));
      FixCheckProperties.ASSERTION_GENERATOR = prop.getProperty("assertion-generator");
      if (prop.getProperty("assertion-generator") == null) {
        System.out.println("Error: assertion-generator property is required");
        System.exit(1);
      } else {
        FixCheckProperties.ASSERTION_GENERATOR = AssertionGeneratorProperty.parseOption(prop.getProperty("assertion-generator"));
      }
      System.out.println("classpath: " + FixCheckProperties.FULL_CLASSPATH);
      System.out.println("test classes path: " + FixCheckProperties.TEST_CLASSES_PATH);
      System.out.println("test class: " + FixCheckProperties.TEST_CLASS);
      System.out.println("test class methods: " + String.join(", ", FixCheckProperties.TEST_CLASS_METHODS));
      System.out.println("test classes sources: " + FixCheckProperties.TEST_CLASS_SRC_DIR);
      System.out.println("inputs class: " + FixCheckProperties.INPUTS_CLASS);
      System.out.println("original failure log: " + FixCheckProperties.ORIGINAL_FAILURE_LOG);
      System.out.println();

    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Setup all Properties.
   */
  public static void setup() {
    // Load the test class source code
    loadTestClassSourceCode();
    // Initialize the possible input classes for each type
    InputHelper.initializeHelper();
    // Load the original failure content
    loadFailureLog();
  }

  /**
   * Get prefixes from the input test class.
   * @return the list of prefixes.
   */
  public static List<Prefix> getPrefixes() {
    if (TEST_CLASS_SRC == null)
      setup();
    List<Prefix> prefixes = new ArrayList<>();
    // Loop over the methods of the test class compilation unit
    String className = TEST_CLASS_SRC.getPrimaryTypeName().get();
    Optional<ClassOrInterfaceDeclaration> classDeclarationOpt = TEST_CLASS_SRC.getClassByName(className);
    if (!classDeclarationOpt.isPresent()) {
      System.out.println("!!! Test class not found: "+ TEST_CLASS+ ". It is present?");
      throw new IllegalArgumentException("Test class not found: "+ TEST_CLASS);
    }
    ClassOrInterfaceDeclaration classDeclaration = classDeclarationOpt.get();
    List<MethodDeclaration> methods = TEST_CLASS_SRC.findAll(MethodDeclaration.class);
    for (MethodDeclaration method : methods) {
      // We don't want to process init methods
      if (method.getNameAsString().equals("<init>")) continue;
      // We don't want to process methods that are not test methods
      if (!isTestMethod(method.getNameAsString())) continue;
      // Create the prefix
      prefixes.add(new Prefix(method, TEST_CLASS_SRC));
    }

    if (prefixes.size() == 0) {
      System.out.println("!!! No prefixes found in test class matching the test methods: "+ Arrays.toString(TEST_CLASS_METHODS));
    }
    PREFIXES_IN_TEST_CLASS = prefixes.size();
    return prefixes;
  }

  /**
   * Returns true if the given method is an input to analyze.
   */
  private static boolean isTestMethod(String methodName) {
    for (String testMethod : TEST_CLASS_METHODS) {
      if (methodName.equals(testMethod)) return true;
    }
    return false;
  }

  /**
   * Load the source code of the test class.
   */
  private static void loadTestClassSourceCode() {
    if (TEST_CLASS_SRC_DIR == null) {
      System.out.println("!!! TEST_CLASS_SRC_DIR is null, cannot load test class source code");
    } else {
      SourceRoot sourceRoot = new SourceRoot(Paths.get(TEST_CLASS_SRC_DIR));
      // Split class name from the last dot
      String[] classParts = TEST_CLASS.split("\\.");
      TEST_CLASS_SIMPLE_NAME = classParts[classParts.length - 1];
      TEST_CLASS_PACKAGE_NAME = TEST_CLASS.substring(0, TEST_CLASS.length() - TEST_CLASS_SIMPLE_NAME.length() - 1);
      TEST_CLASS_SRC = sourceRoot.parse(TEST_CLASS_PACKAGE_NAME, TEST_CLASS_SIMPLE_NAME + ".java");
    }
  }

  /**
   * Load the content of the original failure.
   */
  private static void loadFailureLog() {
    ORIGINAL_FAILURE_STR = "";
    try {
      if (ORIGINAL_FAILURE_LOG != null) {
        List<String> allLines = Files.readAllLines(Paths.get(ORIGINAL_FAILURE_LOG));
        if (ORIGINAL_FAILURE_LOG.contains("DefectRepairing")) // Remove the first line if it is DefectRepairing subject
          allLines.remove(0);
        for (String line : allLines) {
          if (line.contains("at sun.reflect.NativeMethodAccessorImpl.invoke0")) // Everything after this line is not related to the actual test
            break;
          ORIGINAL_FAILURE_STR += line + "\n";
        }
      }
    } catch (IOException e) {
      System.out.println("Error opening file: "+ORIGINAL_FAILURE_LOG);
      System.out.println(e.getMessage());
    }
  }

  /**
   * Get the report file name
   */
  public static String getReportFileName() {
    return OUTPUT_DIR + "/" + OUTPUT_REPORT;
  }

  /**
   * Get the scores file name
   */
  public static String geScoresFileName() {
    return OUTPUT_DIR + "/" + OUTPUT_SCORES;
  }

  /**
   * Get the folder name for passing prefixes
   */
  public static String getPassingTestsDir() {
    return OUTPUT_DIR + "/" + OUTPUT_PASSING_PREFIXES_DIR;
  }

  /**
   * Get the folder name for failing prefixes
   */
  public static String getFailingTestsDir() {
    return OUTPUT_DIR + "/" + OUTPUT_FAILING_PREFIXES_DIR;
  }

  /**
   * Get the folder name for non compiling prefixes
   */
  public static String getNonCompilingTestsDir() {
    return OUTPUT_DIR + "/" + OUTPUT_NONCOMPILING_PREFIXES_DIR;
  }

}