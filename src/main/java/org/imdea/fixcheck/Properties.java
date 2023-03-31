package org.imdea.fixcheck;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import org.imdea.fixcheck.prefix.Prefix;
import org.imdea.fixcheck.transform.input.InputHelper;
import soot.*;
import soot.options.Options;

import java.nio.file.Paths;
import java.util.*;

/**
 * Properties class: contains properties to configure the FixCheck execution.
 * @author Facundo Molina
 */
public class Properties {

  // General properties
  public static String FULL_CLASSPATH = System.getProperty("java.class.path"); // Full classpath (test classes + dependencies + fixcheck)

  // Properties related to the tests
  public static String TEST_CLASSES_PATH; // Path to where the test classes are located
  public static String TEST_CLASS; // Full name of the test class
  public static SootClass SOOT_TEST_CLASS; // Soot class of the test class
  public static String[] TEST_CLASS_METHODS; // Methods in the test class to analyze

  public static String TEST_CLASS_SRC_DIR; // Source file dir for tests
  public static CompilationUnit TEST_CLASS_SRC; // Source file for tests
  public static int PREFIXES_IN_TEST_CLASS = 0; // Number of prefixes in the test class

  // Properties related to the target class
  public static String TARGET_CLASS; // Full name of the target class

  // Properties related to prefixes
  public static int PREFIX_VARIATIONS = 1; // Total number of prefix variations to generate

  // Properties related to inputs
  public static String INPUTS_CLASS; // Full name of the inputs class
  public static SootClass SOOT_INPUTS_CLASS; // Soot class of the inputs class

  // Output files
  public static String OUTPUT_DIR = "fixcheck-output"; // Output directory
  public static String OUTPUT_REPORT = "report.csv"; // Output file for stats

  /**
   * Setup all Properties.
   */
  public static void setup() {
    // Setup Soot
    G.reset();
    Options.v().set_prepend_classpath(true);
    Options.v().set_allow_phantom_refs(true);
    //Options.v().setPhaseOption("jb", "use-original-names:true");
    Options.v().set_soot_classpath(FULL_CLASSPATH);
    // Load the test class
    SootClass sc = Scene.v().loadClassAndSupport(TEST_CLASS);
    sc.setApplicationClass();
    Scene.v().loadNecessaryClasses();
    // Set the test class
    SOOT_TEST_CLASS = sc;
    // Set the inputs class
    SOOT_INPUTS_CLASS = Scene.v().getSootClass(INPUTS_CLASS);
    // Load the test class source code
    loadTestClassSourceCode();
    // Initialize the possible input classes for each type
    InputHelper.initializeHelper();
  }

  /**
   * Get prefixes from the input test class.
   * @return the list of prefixes.
   */
  public static List<Prefix> getPrefixes() {
    if (SOOT_TEST_CLASS == null)
      setup();
    List<Prefix> prefixes = new ArrayList<>();
    for (SootMethod method : SOOT_TEST_CLASS.getMethods()) {
      // We don't want to process init methods
      if (method.getName().equals("<init>")) continue;
      // We don't want to process methods that are not test methods
      if (!isTestMethod(method.getName())) continue;
      // Remove assert statements from the method containing org.junit.Assert or org.junit.TestCase*assert
      removeAssertionsFromMethod(method);
      // Create the prefix
      prefixes.add(new Prefix(method, SOOT_TEST_CLASS));
    }
    if (prefixes.size() == 0) {
      System.out.println("!!! No prefixes found in test class matching the test methods: "+ Arrays.toString(TEST_CLASS_METHODS));
    }
    PREFIXES_IN_TEST_CLASS = prefixes.size();
    return prefixes;
  }

  /**
   * Remove assert statements from the method containing org.junit.Assert
   */
  private static void removeAssertionsFromMethod(SootMethod method) {
    // This is enough to remove the assertions on JUnit 4
    method.retrieveActiveBody().getUnits().removeIf(unit -> unit.toString().contains("org.junit.Assert"));
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
      TEST_CLASS_SRC = sourceRoot.parse(SOOT_TEST_CLASS.getPackageName(), SOOT_TEST_CLASS.getShortName() + ".java");
    }
  }

  /**
   * Get the report file name
   */
  public static String getReportFileName() {
    return OUTPUT_DIR + "/" + OUTPUT_REPORT;
  }

}