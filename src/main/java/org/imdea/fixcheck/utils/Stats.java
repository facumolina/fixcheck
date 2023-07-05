package org.imdea.fixcheck.utils;

/**
 * Stats class: provide methods to get statistics.
 * @author Facundo Molina
 */
public class Stats {

  // Prefixes data
  public static int TOTAL_PREFIXES = 0;
  public static int TOTAL_CRASHING_PREFIXES = 0;
  public static int TOTAL_ASSERTION_FAILING_PREFIXES = 0;
  public static int TOTAL_PASSING_PREFIXES = 0;
  public static int TOTAL_NON_COMPILING_PREFIXES = 0;

  // Time data
  public static long MS_PREFIXES_GENERATION = 0;
  public static long MS_ASSERTIONS_GENERATION = 0;
  public static long MS_RUNNING_PREFIXES = 0;

}
