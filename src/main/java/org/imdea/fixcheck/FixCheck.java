package org.imdea.fixcheck;

import org.imdea.fixcheck.loader.TestLoader;
import org.imdea.fixcheck.prefix.Prefix;

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
  }

}
