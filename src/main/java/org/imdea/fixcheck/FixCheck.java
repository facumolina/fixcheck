package org.imdea.fixcheck;

import org.imdea.fixcheck.assertion.*;
import org.imdea.fixcheck.checker.FailureChecker;
import org.imdea.fixcheck.prefix.Prefix;
import org.imdea.fixcheck.properties.FixCheckProperties;
import org.imdea.fixcheck.runner.PrefixRunner;
import org.imdea.fixcheck.transform.PrefixTransformer;
import org.imdea.fixcheck.transform.input.InputHelper;
import org.imdea.fixcheck.transform.input.InputTransformer;
import org.imdea.fixcheck.utils.Stats;
import org.imdea.fixcheck.writer.PrefixWriter;
import org.imdea.fixcheck.writer.ReportWriter;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import org.apache.commons.cli.*;

import java.io.*;
import java.util.*;

/**
 * FixCheck class: main class.
 * @author Facundo Molina
 */
public class FixCheck {

  private static Set<Prefix> crashingPrefixes = new HashSet<>(); // Built prefixes that crash when executed
  private static Set<Prefix> assertionFailingPrefixes = new HashSet<>(); // Built prefixes that fail the assertion when executed
  private static Set<Prefix> passingPrefixes = new HashSet<>(); // Built prefixes that pass the assertion when executed
  private static Set<Prefix> nonCompilingPrefixes = new HashSet<>(); // Built prefixes that do not compile

  private static Map<Prefix, Double> failingPrefixesScores = new HashMap<>(); // Failing prefixes with their scores (similarity to the original failure)

  /**
   * Build the command line options
   * @return the command line options
   */
  private static Options buildOptions() {
    Options options = new Options();

    // Properties file
    Option propertiesFileOpt = new Option("p", "properties-file", true, "Properties file");
    propertiesFileOpt.setRequired(false);
    options.addOption(propertiesFileOpt);

    // Add help option
    Option helpOpt = new Option("h", "help", false, "Print this message");
    helpOpt.setRequired(false);
    options.addOption(helpOpt);

    return options;
  }

  private static void readArgs(String[] args) {
    Options options = buildOptions();

    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd = null;//not a good practice, it serves it purpose

    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("java -jar fixcheck-all.jar", options);
      System.exit(1);
    }

    if (cmd.hasOption("help")) {
      formatter.printHelp("java -jar fixcheck-all.jar", options);
      System.exit(0);
    }

    String propertiesFile = System.getProperty("user.dir") + "/fixcheck.properties";
    if (cmd.hasOption("properties-file")) {
      propertiesFile = cmd.getOptionValue("properties-file");
    }

    FixCheckProperties.loadProperties(propertiesFile);
  }

  public static void main(String[] args) {
    System.out.println("> FixCheck");
    readArgs(args);

    System.out.println("====== SETUP ======");
    // Loading the prefixes to analyze
    List<Prefix> prefixes = FixCheckProperties.getPrefixes();
    System.out.println("loaded prefixes: " + prefixes.size());
    System.out.println("prefixes to produce: " + FixCheckProperties.PREFIX_VARIATIONS);
    System.out.println("assertions generation: " + FixCheckProperties.ASSERTION_GENERATOR);
    System.out.println("inputs used in the prefixes:");
    // Show the inputs collected in the providers
    InputHelper.PROVIDERS.forEach((k,v) -> { System.out.println("  " + k + ": " + v);});
    System.out.println();

    try {
      System.out.println("====== GENERATION ======");
      generateSimilarPrefixes(prefixes, FixCheckProperties.PREFIX_VARIATIONS);
    } catch (ClassNotFoundException | IOException e) {
      System.out.println("Error generating similar prefixes!!");
      System.out.println(e.getMessage());
    }

    System.out.println("====== OUTPUT ======");
    System.out.println("total prefixes: " + Stats.TOTAL_PREFIXES);
    System.out.println("non-compiling: " + Stats.TOTAL_NON_COMPILING_PREFIXES);
    System.out.println("passing: " + Stats.TOTAL_PASSING_PREFIXES);
    System.out.println("crashing: " + Stats.TOTAL_CRASHING_PREFIXES);
    System.out.println("assertion failing: " + Stats.TOTAL_ASSERTION_FAILING_PREFIXES);
    System.out.println();

    generateOutputFiles();

    System.out.println("Done!");
  }

  /**
   * Generate a list of similar prefixes for each prefix in the list.
   * @param prefixes list of prefixes to generate similar prefixes
   * @param n number of similar prefixes to generate for each prefix
   * @return list of similar prefixes
   */
  public static List<Prefix> generateSimilarPrefixes(List<Prefix> prefixes, int n) throws ClassNotFoundException, IOException {
    List<Prefix> generatedPrefixes = new ArrayList<>();
    PrefixTransformer prefixTransformer = new InputTransformer();
    AssertionGenerator assertionGenerator = getAssertionGenerator();
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

        // Run the prefix.
        System.out.println("---> prefix execution without assertions");
        start = System.currentTimeMillis();
        PrefixRunner.runPrefix(newPrefix);
        elapsedTime = System.currentTimeMillis() - start;
        Stats.MS_RUNNING_PREFIXES += elapsedTime;
        Result result = newPrefix.getExecutionResult();
        if (result.getFailureCount() == 0) {
          // If it doesn't crash, generate the assertions
          start = System.currentTimeMillis();
          System.out.println("---> assertion generator: " + assertionGenerator.getClass().getSimpleName());
          assertionGenerator.generateAssertions(newPrefix);
          elapsedTime = System.currentTimeMillis() - start;
          System.out.println("---> time: " + elapsedTime + "ms");
          System.out.println();
          Stats.MS_ASSERTIONS_GENERATION += elapsedTime;

          // Run the transformed prefix
          System.out.println("---> prefix execution with assertions");
          start = System.currentTimeMillis();
          PrefixRunner.runPrefix(newPrefix);
          elapsedTime = System.currentTimeMillis() - start;
          Stats.MS_RUNNING_PREFIXES += elapsedTime;
        }
        savePrefix(newPrefix);
        generatedPrefixes.add(newPrefix);
      }
    }

    // Save stats
    Stats.TOTAL_PREFIXES = generatedPrefixes.size();
    Stats.TOTAL_CRASHING_PREFIXES = crashingPrefixes.size();
    Stats.TOTAL_ASSERTION_FAILING_PREFIXES = assertionFailingPrefixes.size();
    Stats.TOTAL_PASSING_PREFIXES = passingPrefixes.size();
    Stats.TOTAL_NON_COMPILING_PREFIXES = nonCompilingPrefixes.size();

    return generatedPrefixes;
  }

  /**
   * Get the assertion generator to use
   */
  private static AssertionGenerator getAssertionGenerator() {
    String assertionGeneratorClassName = FixCheckProperties.ASSERTION_GENERATOR;
    try {
      return (AssertionGenerator) Class.forName(assertionGeneratorClassName).getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new IllegalArgumentException("Unknown assertion generator: " + assertionGeneratorClassName);
    }
  }

  /**
   * Save the prefix in the corresponding set
   * @param prefix Prefix to save
   */
  private static void savePrefix(Prefix prefix) {
    Result result = prefix.getExecutionResult();
    if (result == null) {
      System.out.println("---> prefix did not compile");
      nonCompilingPrefixes.add(prefix);
    } else {
      if (result.getFailureCount() > 0) {
        // If some failure is an assertion error, then the prefix failed the assertion
        if (result.getFailures().stream().anyMatch(f -> f.getException() instanceof AssertionError)) {
          System.out.println("---> prefix failed assertion");
          assertionFailingPrefixes.add(prefix);
        } else {
          System.out.println("---> prefix crashed");
          crashingPrefixes.add(prefix);
        }
        measureFailureReason(prefix, result);
      } else {
        System.out.println("---> prefix passed");
        passingPrefixes.add(prefix);
      }
    }
    System.out.println();
  }

  /**
   * Measure the similarity of the given failing result with the original failure
   * @param prefix is the executed prefix
   * @param result is a failing result
   */
  private static void measureFailureReason(Prefix prefix, Result result) {
    double max=0;
    for (Failure failure : result.getFailures()) {
      max = Math.max(FailureChecker.similarity(failure, FixCheckProperties.ORIGINAL_FAILURE_STR),max);
    }
    System.out.println("---> failure similarity: "+max);
    failingPrefixesScores.put(prefix, max);
  }

  /**
   * Generate the output files
   */
  private static void generateOutputFiles() {
    String reportFile = FixCheckProperties.getReportFileName();
    System.out.println("report file: " + reportFile);
    ReportWriter.writeReport(reportFile);
    String nonCompilingPrefixesDir = FixCheckProperties.getNonCompilingTestsDir();
    System.out.println("non compiling prefixes: "+nonCompilingPrefixesDir);
    PrefixWriter.saveNonCompilingPrefixes(nonCompilingPrefixes, nonCompilingPrefixesDir);
    String passingPrefixesDir = FixCheckProperties.getPassingTestsDir();
    System.out.println("passing prefixes: "+passingPrefixesDir);
    PrefixWriter.savePassingPrefixes(passingPrefixes, passingPrefixesDir);
    String failingPrefixesDir = FixCheckProperties.getFailingTestsDir();
    System.out.println("failing prefixes: "+failingPrefixesDir);
    PrefixWriter.saveFailingPrefixes(crashingPrefixes, failingPrefixesDir);
    PrefixWriter.saveFailingPrefixes(assertionFailingPrefixes, failingPrefixesDir);
    String scoresFile = FixCheckProperties.geScoresFileName();
    System.out.println("scores file: "+scoresFile);
    PrefixWriter.saveScores(failingPrefixesScores, scoresFile);
  }

}