package org.imdea.fixcheck.assertion;

import com.github.javaparser.ast.body.MethodDeclaration;
import org.imdea.fixcheck.prefix.Prefix;

/**
 * AssertTrueGenerator class: simple generator including an assert(true) at the end of the prefix, just for testing.
 *
 * @author Facundo Molina <facundo.molina@imdea.org>
 */
public class AssertTrueGenerator extends AssertionGenerator {

    @Override
    public void generateAssertions(Prefix prefix) {
      // Take the method and units
      MethodDeclaration method = prefix.getMethod();
      // Add the assertTrue at the end of the prefix
      method.getBody().get().addStatement("assertTrue(true);");
    }

}
