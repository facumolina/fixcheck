package org.imdea.fixcheck;

import org.imdea.fixcheck.loader.TestLoader;
import org.imdea.fixcheck.prefix.Prefix;
import soot.Body;
import soot.SootClass;
import soot.SootMethod;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * FixCheck class: main class.
 *
 * @author Facundo Molina <facundo.molina@imdea.org>
 */
public class FixCheck {
  public static void main(String[] args) {
    System.out.println("> FixCheck");
    String targetTestsPath = args[0];
    String targetTests = args[1];
    System.out.println("target tests path: " + targetTestsPath);
    System.out.println("bug revealing tests: " + targetTests);
    System.out.println();

    // Loading the prefixes to analyze
    List<Prefix> prefixes = TestLoader.loadPrefixes(targetTestsPath, targetTests);
    System.out.println("prefixes: " + prefixes.size());

    // TODO: analyze the prefixes
    // TODO: first approach: generate similar prefixes by changing the 'inputs' in the given prefixes
  }

  public static List<Prefix> generateSimilarPrefixes(List<Prefix> prefixes, int n) throws ClassNotFoundException {
    String inputClass = "java.util.Date";
    String inputType = "java.lang.Object";
    Class<?> inputClassType = Class.forName(inputType);
    List<Prefix> similarPrefixes = new ArrayList<>();
    for (Prefix prefix : prefixes) {
      SootMethod method = prefix.getMethod();
      SootClass sootClass = prefix.getMethodClass();
      for (int i=0; i < n; i++) {
        // Generate n similar prefixes

        break;
      }
    }
    return similarPrefixes;
  }

}