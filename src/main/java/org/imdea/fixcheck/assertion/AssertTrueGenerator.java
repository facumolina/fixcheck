package org.imdea.fixcheck.assertion;

import org.imdea.fixcheck.prefix.Prefix;
import soot.*;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;

/**
 * AssertTrueGenerator class: simple generator including an assert(true) at the end of the prefix, just for testing.
 * @author Facundo Molina
 */
public class AssertTrueGenerator extends AssertionGenerator {

    @Override
    public void generateAssertions(Prefix prefix) {
      // Take the method and units
      SootMethod method = prefix.getMethod();
      Body body = method.retrieveActiveBody();
      UnitPatchingChain units = body.getUnits();
      // Define local for org.junit.Assert
      Local assertRef = Jimple.v().newLocal("assertRef", RefType.v("org.junit.Assert"));
      body.getLocals().add(assertRef);
      // Add the assertFalse at the end of the prefix
      SootMethod assertMethod = Scene.v().getMethod("<org.junit.Assert: void assertTrue(boolean)>");
      Unit unit = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(assertMethod.makeRef(), IntConstant.v(1)));
      units.removeLast(); // Remove the return statement
      units.addLast(unit);
      units.addLast(Jimple.v().newReturnVoidStmt());
    }

}
