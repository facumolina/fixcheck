package org.imdea.fixcheck.loader;


import java.util.ArrayList;
import java.util.List;

import org.imdea.fixcheck.Properties;
import org.imdea.fixcheck.prefix.Prefix;
import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;

/**
 * TestLoader class: provides methods to load a test class.
 * @author Facundo Molina
 */
public class TestLoader {

  /**
   * Setup the TestLoader.
   */
  public static SootClass setup() {
    System.out.println("Setting up TestLoader");
    G.reset();
    Options.v().set_prepend_classpath(true);
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_soot_classpath(Properties.TEST_CLASSES_PATH);
    SootClass sc = Scene.v().loadClassAndSupport(Properties.TEST_CLASS);
    sc.setApplicationClass();
    Scene.v().loadNecessaryClasses();
    return Scene.v().getSootClass(Properties.TEST_CLASS);
  }

  /**
   * Load prefixes from the input test class.
   * @return the list of prefixes.
   */
  public static List<Prefix> loadPrefixes() {
    SootClass sootClass = setup();
    List<Prefix> prefixes = new ArrayList<>();
    for (SootMethod method : sootClass.getMethods()) {
      // We don't want to process init methods
      if (method.getName().equals("<init>")) continue;
      // Remove assert statements from the method
      method.retrieveActiveBody().getUnits().removeIf(unit -> unit.toString().startsWith("assert") || unit.toString().contains("org.junit.Assert"));
      prefixes.add(new Prefix(method, sootClass));
    }
    return prefixes;
  }

}
