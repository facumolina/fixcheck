package org.imdea.fixcheck.loader;


import java.util.ArrayList;
import java.util.List;

import org.imdea.fixcheck.prefix.Prefix;
import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;

/**
 * TestLoader class: provides methods to load a test class.
 *
 * @author Facundo Molina <facundo.molina@imdea.org>
 */
public class TestLoader {


  /**
   * Setup the TestLoader.
   */
  public static SootClass setup(String classesPath, String testClass) {
    System.out.println("Setting up TestLoader");
    G.reset();
    Options.v().set_prepend_classpath(true);
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_soot_classpath(classesPath);
    SootClass sc = Scene.v().loadClassAndSupport(testClass);
    sc.setApplicationClass();
    Scene.v().loadNecessaryClasses();
    return Scene.v().getSootClass(testClass);
  }

  /**
   * Load prefixes from the given test class.
   * @param testClass is the name of the test class to load the prefixes from.
   * @return the list of prefixes.
   */
  public static List<Prefix> loadPrefixes(String classesPath, String testClass) {
    SootClass sootClass = setup(classesPath, testClass);
    List<Prefix> prefixes = new ArrayList<>();
    for (SootMethod method : sootClass.getMethods()) {
      if (method.getName().equals("<init>")) continue;
      prefixes.add(new Prefix(method, sootClass));
    }
    return prefixes;
  }

}
