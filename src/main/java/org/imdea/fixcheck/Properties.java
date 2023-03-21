package org.imdea.fixcheck;

import org.imdea.fixcheck.prefix.Prefix;
import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;

import java.util.ArrayList;
import java.util.List;

/**
 * Properties class: contains properties to configure the FixCheck execution.
 * @author Facundo Molina
 */
public class Properties {

  public static String TEST_CLASSES_PATH; // Full path to the test classes + dependencies + fixcheck
  public static String TEST_CLASS; // Full name of the test class
  public static SootClass SOOT_TEST_CLASS; // Soot class of the test class

  public static String TARGET_CLASS; // Full name of the target class
  public static int PREFIX_VARIATIONS = 1; // Total number of prefix variations to generate

  /**
   * Setup the TestLoader.
   */
  public static void setup() {
    System.out.println("Setting up Properties");
    G.reset();
    Options.v().set_prepend_classpath(true);
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_soot_classpath(TEST_CLASSES_PATH);
    SootClass sc = Scene.v().loadClassAndSupport(TEST_CLASS);
    sc.setApplicationClass();
    Scene.v().loadNecessaryClasses();
    SOOT_TEST_CLASS = sc;
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
      // Remove assert statements from the method
      method.retrieveActiveBody().getUnits().removeIf(unit -> unit.toString().startsWith("assert") || unit.toString().contains("org.junit.Assert"));
      prefixes.add(new Prefix(method, SOOT_TEST_CLASS));
    }
    return prefixes;
  }
}
