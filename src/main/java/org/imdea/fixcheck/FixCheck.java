package org.imdea.fixcheck;

import org.imdea.fixcheck.assertion.AssertTrueGenerator;
import org.imdea.fixcheck.assertion.AssertionGenerator;
import org.imdea.fixcheck.prefix.Prefix;
import org.imdea.fixcheck.runner.PrefixRunner;
import org.imdea.fixcheck.transform.PrefixTransformer;
import org.imdea.fixcheck.transform.input.InputHelper;
import org.imdea.fixcheck.transform.input.InputTransformer;
import org.imdea.fixcheck.utils.Stats;
import org.imdea.fixcheck.writer.ReportWriter;
import org.junit.runner.Result;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * FixCheck class: main class.
 * @author Facundo Molina
 */
public class FixCheck {

  private static Set<Prefix> crashingPrefixes = new HashSet<>(); // Built prefixes that crash when executed
  private static Set<Prefix> assertionFailingPrefixes = new HashSet<>(); // Built prefixes that fail the assertion when executed
  private static Set<Prefix> passingPrefixes = new HashSet<>(); // Built prefixes that pass the assertion when executed

  private static void readArgs(String[] args) {
    Properties.TEST_CLASSES_PATH = args[0];
    Properties.TEST_CLASS = args[1];
    Properties.TEST_CLASS_METHODS = args[2].split(":");
    Properties.TEST_CLASS_SRC_DIR = args[3];
    Properties.TARGET_CLASS = args[4];
    Properties.INPUTS_CLASS = args[5];
    Properties.PREFIX_VARIATIONS = Integer.parseInt(args[6]);
    System.out.println("classpath: " + Properties.FULL_CLASSPATH);
    System.out.println("test classes path: " + Properties.TEST_CLASSES_PATH);
    System.out.println("test class: " + Properties.TEST_CLASS);
    System.out.println("test class methods: " + String.join(", ", Properties.TEST_CLASS_METHODS));
    System.out.println("test classes sources: " + Properties.TEST_CLASS_SRC_DIR);
    System.out.println("target class: " + Properties.TARGET_CLASS);
    System.out.println("inputs class: " + Properties.INPUTS_CLASS);
    System.out.println();
  }

  public static void main(String[] args) {
    System.out.println("> FixCheck");
    readArgs(args);

    System.out.println("====== SETUP ======");
    // Loading the prefixes to analyze
    List<Prefix> prefixes = Properties.getPrefixes();
    System.out.println("loaded prefixes: " + prefixes.size());
    System.out.println("prefixes to produce: " + Properties.PREFIX_VARIATIONS);
    System.out.println("inputs used in the prefixes:");
    // Show the inputs collected in the providers
    InputHelper.PROVIDERS.forEach((k,v) -> { System.out.println("  " + k + ": " + v);});
    System.out.println();

    try {
      System.out.println("====== GENERATION ======");
      generateSimilarPrefixes(prefixes, Properties.PREFIX_VARIATIONS);
    } catch (ClassNotFoundException | IOException e) {
      System.out.println("Error generating similar prefixes!!");
      System.out.println(e.getMessage());
    }

    System.out.println("====== OUTPUT ======");
    System.out.println("total prefixes: " + Stats.TOTAL_PREFIXES);
    System.out.println("passing: " + Stats.TOTAL_PASSING_PREFIXES);
    System.out.println("crashing: " + Stats.TOTAL_CRASHING_PREFIXES);
    System.out.println("assertion failing: " + Stats.TOTAL_ASSERTION_FAILING_PREFIXES);
    System.out.println();

    generateOutputFiles();

    System.out.println("Done!");
  }

  public static List<Prefix> generateSimilarPrefixes(List<Prefix> prefixes, int n) throws ClassNotFoundException, IOException {
    List<Prefix> generatedPrefixes = new ArrayList<>();
    PrefixTransformer prefixTransformer = new InputTransformer();
    //AssertionGenerator assertionGenerator = new TextDavinci003();
    AssertionGenerator assertionGenerator = new AssertTrueGenerator();
    for (Prefix prefix : prefixes) {
      // Generate n similar prefixes
      for (int i=1; i <= n; i++) {

        // Generate the prefix
        System.out.println("PREFIX " + i + " of " + n);
        long start = System.currentTimeMillis();
        System.out.println("---> transformer: " + prefixTransformer.getClass().getSimpleName());
        Prefix newPrefix = prefixTransformer.transform(prefix);
        System.out.println("---> generated prefix: " + newPrefix.fullName());
        System.out.println("---> transformation: " + prefixTransformer.getLastTransformation());
        long elapsedTime = System.currentTimeMillis() - start;
        System.out.println("---> time: " + elapsedTime + "ms");
        System.out.println();
        Stats.MS_PREFIXES_GENERATION += elapsedTime;

        // Generate the assertions for the prefix

        start = System.currentTimeMillis();
        System.out.println("---> assertion generator: " + assertionGenerator.getClass().getSimpleName());
        assertionGenerator.generateAssertions(newPrefix);
        elapsedTime = System.currentTimeMillis() - start;
        System.out.println("---> time: " + elapsedTime + "ms");
        System.out.println();
        Stats.MS_ASSERTIONS_GENERATION += elapsedTime;

        // Run the transformed prefix
        start = System.currentTimeMillis();
        Result result = PrefixRunner.runPrefix(newPrefix);
        elapsedTime = System.currentTimeMillis() - start;
        Stats.MS_RUNNING_PREFIXES += elapsedTime;
        savePrefix(newPrefix, result);
        generatedPrefixes.add(newPrefix);
        System.out.println();
      }
    }

    // Save stats
    Stats.TOTAL_PREFIXES = generatedPrefixes.size();
    Stats.TOTAL_CRASHING_PREFIXES = crashingPrefixes.size();
    Stats.TOTAL_ASSERTION_FAILING_PREFIXES = assertionFailingPrefixes.size();
    Stats.TOTAL_PASSING_PREFIXES = passingPrefixes.size();

    return generatedPrefixes;
  }

  /**
   * Save the prefix in the corresponding set
   * @param prefix Prefix to save
   * @param result Result of the prefix execution
   */
  private static void savePrefix(Prefix prefix, Result result) {
    if (result.getFailureCount() > 0) {
      // If some failure is an assertion error, then the prefix failed the assertion
      if (result.getFailures().stream().anyMatch(f -> f.getException() instanceof AssertionError)) {
        System.out.println("---> prefix failed assertion");
        assertionFailingPrefixes.add(prefix);
      } else {
        System.out.println("---> prefix crashed");
        crashingPrefixes.add(prefix);
      }
    } else {
      System.out.println("---> prefix passed");
      passingPrefixes.add(prefix);
    }
  }

  /**
   * Generate the output files
   */
  private static void generateOutputFiles() {
    String reportFile = Properties.getReportFileName();
    System.out.println("report file: " + reportFile);
    ReportWriter.writeReport(reportFile);
  }

}