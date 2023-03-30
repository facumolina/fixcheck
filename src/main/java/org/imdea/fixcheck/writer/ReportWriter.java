package org.imdea.fixcheck.writer;

import com.opencsv.CSVWriter;
import org.imdea.fixcheck.FixCheck;
import org.imdea.fixcheck.Properties;
import org.imdea.fixcheck.utils.Stats;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * ReportWriter class: provide methods to write a summary report of the execution.
 */
public class ReportWriter {

  private static final String[] reportHeader = {"test_class", "input_prefixes", "inputs_class", "target_class", "prefixes_gen_time", "assertions_gen_time", "output_prefixes", "passing_prefixes", "crashing_prefixes", "assertion_failing_prefixes"};

  public static void writeReport(String reportFileName) {
    try {
      Files.createDirectories(Paths.get(Properties.OUTPUT_DIR));
      File file = new File(reportFileName);
      try (CSVWriter writer = new CSVWriter(new FileWriter(file), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
        writer.writeNext(reportHeader);
        writeReportData(writer);
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Error writing report file: " + reportFileName);
    }
  }

  /**
   * Write the data of the report.
   * @param writer CSVWriter to write the data.
   */
  private static void writeReportData(CSVWriter writer) {
    String[] data = new String[reportHeader.length];
    data[0] = Properties.TEST_CLASS;
    data[1] = String.valueOf(Properties.PREFIXES_IN_TEST_CLASS);
    data[2] = Properties.INPUTS_CLASS;
    data[3] = Properties.TARGET_CLASS;
    data[4] = "tbd";
    data[5] = "tbd";
    data[6] = "tbd";
    data[7] = String.valueOf(Stats.TOTAL_PASSING_PREFIXES);
    data[8] = String.valueOf(Stats.TOTAL_CRASHING_PREFIXES);
    data[9] = String.valueOf(Stats.TOTAL_ASSERTION_FAILING_PREFIXES);
    writer.writeNext(data);
  }

}
