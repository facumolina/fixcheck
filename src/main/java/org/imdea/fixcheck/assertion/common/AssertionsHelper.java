package org.imdea.fixcheck.assertion.common;

import org.imdea.fixcheck.prefix.Prefix;

/**
 * AssertionsHelper class: helper class for assertions.
 * @author Facundo Molina
 */
public class AssertionsHelper {

  /**
   * Append an assertion to the prefix.
   * @param assertions Assertion to append
   * @param prefix Prefix to append the assertion
   */
  public static void appendAssertionsToPrefix(String assertions, Prefix prefix) {
    String[] lines = assertions.split("\\r?\\n"); // Split by lines
    for (String possibleAssertion : lines) {
      if (possibleAssertion.startsWith("//")) continue; // It's a comment
      if (possibleAssertion.trim().isEmpty()) continue; // It's an empty line
      System.out.println("assertion line: "+ possibleAssertion);
    }
  }

}
