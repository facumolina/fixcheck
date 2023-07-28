package org.imdea.fixcheck;

import org.imdea.fixcheck.assertion.*;
import org.imdea.fixcheck.checker.FailureChecker;
import org.imdea.fixcheck.prefix.Prefix;
import org.imdea.fixcheck.runner.PrefixRunner;
import org.imdea.fixcheck.transform.PrefixTransformer;
import org.imdea.fixcheck.transform.input.InputHelper;
import org.imdea.fixcheck.transform.input.InputTransformer;
import org.imdea.fixcheck.utils.Stats;
import org.imdea.fixcheck.writer.PrefixWriter;
import org.imdea.fixcheck.writer.ReportWriter;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

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

  private static void readArgs(String[] args) {
    Properties.TEST_CLASSES_PATH = args[0];
    Properties.TEST_CLASS = args[1];
    Properties.TEST_CLASS_METHODS = args[2].split(":");
    Properties.TEST_CLASS_SRC_DIR = args[3];
    Properties.TARGET_CLASS = args[4];
    Properties.INPUTS_CLASS = args[5];
    Properties.ORIGINAL_FAILURE_LOG = args[6];
    Properties.PREFIX_VARIATIONS = Integer.parseInt(args[7]);
    Properties.ASSERTIONS_GENERATION = args[8];
    System.out.println("classpath: " + Properties.FULL_CLASSPATH);
    System.out.println("test classes path: " + Properties.TEST_CLASSES_PATH);
    System.out.println("test class: " + Properties.TEST_CLASS);
    System.out.println("test class methods: " + String.join(", ", Properties.TEST_CLASS_METHODS));
    System.out.println("test classes sources: " + Properties.TEST_CLASS_SRC_DIR);
    System.out.println("target class: " + Properties.TARGET_CLASS);
    System.out.println("inputs class: " + Properties.INPUTS_CLASS);
    System.out.println("original failure log: " + Properties.ORIGINAL_FAILURE_LOG);
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
    System.out.println("assertions generation: " + Properties.ASSERTIONS_GENERATION);
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
    List<PrefixTransformer> transformers = Arrays.asList(
        new InputTransformer()
    );
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
    if ("no-assertion".equals(Properties.ASSERTIONS_GENERATION))
      return new AssertTrueGenerator();
    if ("previous-assertion".equals(Properties.ASSERTIONS_GENERATION))
      return new UsePreviousAssertGenerator();
    if ("llm-assertion".equals(Properties.ASSERTIONS_GENERATION))
      return new TextDavinci003();
    if ("replit-code-llm".equals(Properties.ASSERTIONS_GENERATION))
      return new GPT4AllReplitCodeLLM();
    if ("llama2-llm-13b".equals(Properties.ASSERTIONS_GENERATION))
      return new LlamaLLM();
    throw new IllegalArgumentException("Unknown assertion generator: " + Properties.ASSERTIONS_GENERATION);
  }

  /**
   * Return a list of transformer randomly chosen from the given list
   */
  public List<PrefixTransformer> getTransformersToApply(List<PrefixTransformer> transformers) {
    List<PrefixTransformer> transformersToApply = new ArrayList<>();
    int n = (int) (Math.random() * transformers.size());
    Collections.shuffle(transformers);
    for (int i=0; i < n; i++) {
      transformersToApply.add(transformers.get(i));
    }
    return transformersToApply;
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
      max = Math.max(FailureChecker.similarity(failure, Properties.ORIGINAL_FAILURE_STR),max);
    }
    System.out.println("---> failure similarity: "+max);
    failingPrefixesScores.put(prefix, max);
  }

  /**
   * Generate the output files
   */
  private static void generateOutputFiles() {
    String reportFile = Properties.getReportFileName();
    System.out.println("report file: " + reportFile);
    ReportWriter.writeReport(reportFile);
    String nonCompilingPrefixesDir = Properties.getNonCompilingTestsDir();
    System.out.println("non compiling prefixes: "+nonCompilingPrefixesDir);
    PrefixWriter.saveNonCompilingPrefixes(nonCompilingPrefixes, nonCompilingPrefixesDir);
    String passingPrefixesDir = Properties.getPassingTestsDir();
    System.out.println("passing prefixes: "+passingPrefixesDir);
    PrefixWriter.savePassingPrefixes(passingPrefixes, passingPrefixesDir);
    String failingPrefixesDir = Properties.getFailingTestsDir();
    System.out.println("failing prefixes: "+failingPrefixesDir);
    PrefixWriter.saveFailingPrefixes(crashingPrefixes, failingPrefixesDir);
    PrefixWriter.saveFailingPrefixes(assertionFailingPrefixes, failingPrefixesDir);
    String scoresFile = Properties.geScoresFileName();
    System.out.println("scores file: "+scoresFile);
    PrefixWriter.saveScores(failingPrefixesScores, scoresFile);
  }

}