package org.imdea.fixcheck;

import org.imdea.fixcheck.assertion.AssertFalseGenerator;
import org.imdea.fixcheck.assertion.AssertionGenerator;
import org.imdea.fixcheck.loader.TestLoader;
import org.imdea.fixcheck.prefix.Prefix;
import org.imdea.fixcheck.runner.PrefixRunner;
import org.imdea.fixcheck.transform.PrefixTransformer;
import org.imdea.fixcheck.transform.input.InputTransformer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FixCheck class: main class.
 * @author Facundo Molina
 */
public class FixCheck {
  public static void main(String[] args) {
    System.out.println("> FixCheck");
    String targetTestsPath = args[0];
    String targetTests = args[1];
    int variations = Integer.parseInt(args[2]);
    System.out.println("target tests path: " + targetTestsPath);
    System.out.println("bug revealing tests: " + targetTests);
    System.out.println("variations to analyze: " + variations);
    System.out.println();

    System.out.println("----- Going to generate prefixes -----");
    // Loading the prefixes to analyze
    List<Prefix> prefixes = TestLoader.loadPrefixes(targetTestsPath, targetTests);
    System.out.println("loaded prefixes: " + prefixes.size());
    System.out.println("prefixes to generate: " + variations);
    System.out.println();

    try {
      generateSimilarPrefixes(prefixes, variations);
    } catch (ClassNotFoundException | IOException e) {
      System.out.println("Error generating similar prefixes!!");
      System.out.println(e.getMessage());
    }

    System.out.println("Done!");
  }

  public static List<Prefix> generateSimilarPrefixes(List<Prefix> prefixes, int n) throws ClassNotFoundException, IOException {
    String inputClass = "java.util.Date";
    String inputType = "java.lang.Object";
    Class<?> inputClassType = Class.forName(inputType);
    List<Prefix> similarPrefixes = new ArrayList<>();
    for (Prefix prefix : prefixes) {
      // Generate n similar prefixes
      for (int i=0; i < n; i++) {
        // Generate the prefix
        PrefixTransformer prefixTransformer = new InputTransformer(prefix);
        Prefix newPrefix = prefixTransformer.transform();
        System.out.println("Generated prefix " + i + ": " + newPrefix.getMethodClass().getName() + "." + newPrefix.getMethod().getName());
        System.out.println(newPrefix.getMethod().getActiveBody());

        // Generate the assertions for the prefix
        AssertionGenerator assertionGenerator = new AssertFalseGenerator(newPrefix);
        assertionGenerator.generateAssertions();

        // Run the transformed prefix
        PrefixRunner.runPrefix(newPrefix);

        similarPrefixes.add(newPrefix);
      }
    }
    return similarPrefixes;
  }

}