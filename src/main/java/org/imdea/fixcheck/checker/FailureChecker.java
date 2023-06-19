package org.imdea.fixcheck.checker;

import org.junit.runner.notification.Failure;

/**
 * FailureChecker class: allows to compare the failure reason of a prefix with the original failure
 *
 * @author Facundo Molina
 */
public class FailureChecker {

  /**
   * Measure the similarity of the given failure with the original failure.
   * The similarity value will be a double from 0 to 1, where 1 represents an exact match.
   * @param failure is the failure being measured
   * @param originalFailure is the original failure in the form a string
   * @return the similarity value
   */
  public static double similarity(Failure failure, String originalFailure) {
    System.out.println("---> Checking similarity");
    System.out.println("Original failure:");
    System.out.println(originalFailure);
    System.out.println("Current failure:");
    String trace = normalizeFailureTrace(failure.getTrace());
    System.out.println(trace);
    double maxLength = Double.max(trace.length(), originalFailure.length());
    return (maxLength - getLevenshteinDistance(trace, originalFailure)) / maxLength;
  }

  private static String normalizeFailureTrace(String trace) {
    String[] lines = trace.split("\n");
    String normalizedTrace = "";
    for (String line : lines) {
      if (line.contains("at sun.reflect.NativeMethodAccessorImpl.invoke0")) // Everything after this line is not related to the actual test
        break;
      normalizedTrace += line+"\n";
    }
    return normalizedTrace;
  }

  /**
   * Returns the Levenshtein Distance of the two given strings
   * @param X first string
   * @param Y second string
   * @return the Levenshtein Distance
   */
  private static int getLevenshteinDistance(String X, String Y) {
    int m = X.length();
    int n = Y.length();

    int[][] T = new int[m + 1][n + 1];
    for (int i = 1; i <= m; i++) {
      T[i][0] = i;
    }
    for (int j = 1; j <= n; j++) {
      T[0][j] = j;
    }

    int cost;
    for (int i = 1; i <= m; i++) {
      for (int j = 1; j <= n; j++) {
        cost = X.charAt(i - 1) == Y.charAt(j - 1) ? 0: 1;
        T[i][j] = Integer.min(Integer.min(T[i - 1][j] + 1, T[i][j - 1] + 1),
            T[i - 1][j - 1] + cost);
      }
    }

    return T[m][n];
  }



}
