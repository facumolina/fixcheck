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

  private static void readArgs(String[] args) {
    Properties.TEST_CLASSES_PATH = args[0];
    Properties.TEST_CLASS = args[1];
    Properties.TARGET_CLASS = args[2];
    Properties.PREFIX_VARIATIONS = Integer.parseInt(args[3]);
  }

  public static void main(String[] args) {
    System.out.println("> FixCheck");
    readArgs(args);
    System.out.println("target tests path: " + Properties.TEST_CLASSES_PATH);
    System.out.println("bug revealing tests: " + Properties.TEST_CLASS);
    System.out.println("target class: " + Properties.TARGET_CLASS);
    System.out.println("prefixes to generate: " + Properties.PREFIX_VARIATIONS);
    System.out.println();

    System.out.println("----- Going to generate prefixes -----");
    // Loading the prefixes to analyze
    List<Prefix> prefixes = TestLoader.loadPrefixes();
    System.out.println("loaded prefixes: " + prefixes.size());
    System.out.println();

    try {
      generateSimilarPrefixes(prefixes, Properties.PREFIX_VARIATIONS);
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