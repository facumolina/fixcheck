package org.imdea.fixcheck.assertion;

import org.imdea.fixcheck.prefix.Prefix;
import soot.*;
import soot.jimple.Jimple;
import soot.jimple.parser.node.TBoolConstant;

/**
 * AssertFalseGenerator class: simple generator including an assert(false) at the end of the prefix, just for testing.
 * @author Facundo Molina
 */
public class AssertFalseGenerator extends AssertionGenerator {

    public AssertFalseGenerator(Prefix prefix) {
      super(prefix);
    }

    @Override
    public void generateAssertions() {
      // Take the method and units
      SootMethod method = prefix.getMethod();
      Body body = method.retrieveActiveBody();
      UnitPatchingChain units = body.getUnits();
      SootClass cl = Scene.v().loadClassAndSupport("org.junit.Assert");
      System.out.println("methods: "+ cl.getMethods());
      // Define local for org.junit.Assert
      Local assertRef = Jimple.v().newLocal("assertRef", RefType.v("org.junit.Assert"));
      body.getLocals().add(assertRef);
      // Add the assertFalse at the end of the prefix
      SootMethod assertMethod = Scene.v().getMethod("<org.junit.Assert: void assertFalse(boolean)>");
      //units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(assertRef, assertMethod.makeRef(), TBoolConstant)
    }

}
