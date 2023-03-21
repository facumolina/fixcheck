package org.imdea.fixcheck.transform.input;

import org.imdea.fixcheck.prefix.Prefix;
import org.imdea.fixcheck.transform.Initializer;
import org.imdea.fixcheck.transform.PrefixTransformer;
import org.imdea.fixcheck.transform.common.TransformationHelper;
import soot.*;
import soot.jimple.*;

/**
 * Input Transformer class: transform a prefix by changing its 'input'.
 * @author Facundo Molina
 */
public class InputTransformer extends PrefixTransformer {

  public InputTransformer(Prefix prefix) {
    super(prefix);
  }

  @Override
  public Prefix transform() {
    Prefix prefix = this.prefix;
    SootClass prefixClass = prefix.getMethodClass();
    SootMethod prefixMethod = prefix.getMethod();
    Body oldBody = prefixMethod.retrieveActiveBody();

    System.out.println("Generating similar prefix with InputTransformer");
    SootClass newClass = Initializer.initializeTransformedClass(prefixClass.getPackageName() + ".SimilarPrefixClass", prefixClass);
    SootMethod newMethod = new SootMethod("similarPrefix", prefixMethod.getParameterTypes(), prefixMethod.getReturnType(), prefixMethod.getModifiers());
    newMethod.addAllTagsOf(prefixMethod);
    newMethod.setExceptions(prefixMethod.getExceptions());
    Body newBody = (Body)oldBody.clone();
    replaceInput(newBody);
    newMethod.setActiveBody(newBody);
    newClass.addMethod(newMethod);

    return new Prefix(newMethod, newClass);
  }

  private void replaceInput(Body body) {
    Local input = TransformationHelper.getLocalWithType(body, "java.util.Date");
    // Define local for the new input
    Local newInput = Jimple.v().newLocal("newInput", RefType.v("java.lang.Integer"));
    body.getLocals().add(newInput);
    // Add call for new input constructor
    AssignStmt assignStmt = Jimple.v().newAssignStmt(newInput, Jimple.v().newNewExpr(RefType.v("java.lang.Integer")));
    SootMethod newInputConstructor = Scene.v().getMethod("<java.lang.Integer: void <init>(int)>");
    InvokeStmt constructorInvoke = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(newInput, newInputConstructor.makeRef(), IntConstant.v(1)));
    // Replace old input constructor with new input constructor
    replaceConstructor(body, input, assignStmt, constructorInvoke);
    // Use the new input in the right place
    TransformationHelper.replace(body, input, newInput);
  }

  private void replaceConstructor(Body body, Local inputToReplace, AssignStmt assignStmt, InvokeStmt constructorInvoke) {
    // First replace the assignment
    for (Unit ut : body.getUnits()) {
      if (ut instanceof AssignStmt) {
        AssignStmt assign = (AssignStmt) ut;
        if (assign.getLeftOp().equals(inputToReplace)) {
          body.getUnits().swapWith(ut, assignStmt);
          break;
        }
      }
    }
    // Then replace the actual constructor call
    for (Unit ut : body.getUnits()) {
      if (ut instanceof InvokeStmt) {
        InvokeExpr invokeExpr = ((InvokeStmt)ut).getInvokeExpr();
        boolean classMatch = invokeExpr.getMethod().getDeclaringClass().getName().equals(inputToReplace.getType().toString());
        boolean methodMatch = invokeExpr.getMethod().getName().equals("<init>");
        if (classMatch && methodMatch) {
            body.getUnits().swapWith(ut, constructorInvoke);
            break;
        }
      }
    }
  }

}