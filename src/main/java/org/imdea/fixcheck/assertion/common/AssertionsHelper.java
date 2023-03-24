package org.imdea.fixcheck.assertion.common;

import org.imdea.fixcheck.prefix.Prefix;
import soot.Scene;
import soot.SootMethod;

import java.util.Arrays;

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

  /**
   * Get the assertion method for the given string.
   * @param assertion String to obtain the assertion method
   * @return Soot method for the assertion
   */
  public static SootMethod getAssertionMethod(String assertion) {
    if (assertion.startsWith("assertTrue"))
      return getAssertTrueMethod();
    throw new RuntimeException("Assertion not supported: " + assertion);
  }
  /**
   * Get the method for the assertTrue statement.
   * @return Soot method for the assertTrue statement
   */
  public static SootMethod getAssertTrueMethod() {
    return Scene.v().getMethod("<org.junit.Assert: void assertTrue(boolean)>");
  }

}
