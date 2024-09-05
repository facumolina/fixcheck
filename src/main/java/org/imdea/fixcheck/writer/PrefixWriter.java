package org.imdea.fixcheck.writer;

import com.opencsv.CSVWriter;
import org.imdea.fixcheck.properties.FixCheckProperties;
import org.imdea.fixcheck.prefix.Prefix;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

/**
 * PrefixWriter class: provide methods to write the generated prefixes.
 */
public class PrefixWriter {

  /**
   * Save the given set of passing prefixes to the specified folder
   * @param passingPrefixes the set of passing prefixes
   * @param outputFolder the set output folder
   */
  public static void savePassingPrefixes(Set<Prefix> passingPrefixes, String outputFolder) {
    try {
      Files.createDirectories(Paths.get(outputFolder));
      for (Prefix prefix : passingPrefixes) {
        savePrefix(prefix, outputFolder);
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Error writing passing prefixes to folder: " + outputFolder);
    }
  }

  /**
   * Save the given set of failing prefixes to the specified folder
   * @param failingPrefixes the set of failing prefixes
   * @param outputFolder the set output folder
   */
  public static void saveFailingPrefixes(Set<Prefix> failingPrefixes, String outputFolder) {
    try {
      Files.createDirectories(Paths.get(outputFolder));
      // Save each prefix, and the failure reason
      for (Prefix prefix : failingPrefixes) {
        savePrefix(prefix, outputFolder);
        Result result = prefix.getExecutionResult();
        int i = 0;
        for (Failure failure : result.getFailures()) {
          String failureFileName = prefix.getClassName()+"-failure"+i+".txt";
          saveFailureContent(failure, failureFileName, outputFolder);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Error writing passing prefixes to folder: " + outputFolder);
    }
  }

  /**
   * Save the given set of non-compiling prefixes to the specified folder
   * @param nonCompilingPrefixes the set of non-compiling prefixes
   * @param outputFolder the set output folder
   */
  public static void saveNonCompilingPrefixes(Set<Prefix> nonCompilingPrefixes, String outputFolder) {
    try {
      Files.createDirectories(Paths.get(outputFolder));
      for (Prefix prefix : nonCompilingPrefixes) {
        savePrefix(prefix, outputFolder);
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Error writing non compiling prefixes to folder: " + outputFolder);
    }
  }

  /**
   * Save a report containing each failing prefix with its corresponding score
   * @param scores is the set of failing prefixes with their corresponding scores
   * @param scoresFileName is the corresponding file name
   */
  public static void saveScores(Map<Prefix, Double> scores, String scoresFileName) {
    try {
      Files.createDirectories(Paths.get(FixCheckProperties.OUTPUT_DIR));
      String[] header = {"prefix","score"};
      File file = new File(scoresFileName);
      try (CSVWriter writer = new CSVWriter(new FileWriter(file), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
        writer.writeNext(header);
        scores.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue())
            .forEach(entry ->
                writer.writeNext(new String[]{ entry.getKey().getClassName(), entry.getValue().toString()})
            );
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Error writing report file: " + scoresFileName);
    }
  }

  /**
   * Save the given prefix in the given location.
   * @param prefix the given prefix
   * @param outputFolder the folder in which to save the prefix
   * @throws IOException if there is a file-related problem when saving the prefix
   */
  private static void savePrefix(Prefix prefix, String outputFolder) throws IOException {
    String prefixClassName = prefix.getClassName();
    String prefixCode = prefix.getClassSourceCode();
    FileWriter prefixFileWriter = new FileWriter(outputFolder + "/" + prefixClassName+".java");
    prefixFileWriter.write(prefixCode);
    prefixFileWriter.close();
  }

  private static void saveFailureContent(Failure failure, String fileName, String outputFolder) throws IOException {
    String failureContent = failure.getTrace();
    FileWriter prefixFileWriter = new FileWriter(outputFolder + "/" + fileName);
    prefixFileWriter.write(failureContent);
    prefixFileWriter.close();
  }

}
