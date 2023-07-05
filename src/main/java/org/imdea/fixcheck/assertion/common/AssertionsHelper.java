package org.imdea.fixcheck.assertion.common;

import com.github.javaparser.ast.body.MethodDeclaration;
import org.imdea.fixcheck.prefix.Prefix;

import java.util.List;

/**
 * AssertionsHelper class: helper class for assertions.
 * @author Facundo Molina
 */
public class AssertionsHelper {

  /**
   * Append an assertion to the prefix.
   * @param assertions Assertions to append
   * @param prefix Prefix to append the assertion
   */
  public static void appendAssertionsToPrefix(List<String> assertions, Prefix prefix) {
    // Take the method
    MethodDeclaration method = prefix.getMethod();
    for (String assertion : assertions) {
      // Add the assertTrue at the end of the prefix
      try {
      method.getBody().get().addStatement(assertion);
      } catch (Exception e) {
        System.out.println("Error adding assertion: " + assertion);
        System.out.println("Error: " + e.getMessage());
      }
    }
  }

}
