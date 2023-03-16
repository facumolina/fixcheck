package org.imdea.fixcheck.transform.input;

import org.imdea.fixcheck.prefix.Prefix;
import org.imdea.fixcheck.transform.Initializer;
import org.imdea.fixcheck.transform.PrefixTransformer;
import soot.Body;
import soot.SootClass;
import soot.SootMethod;

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
    SootClass newClass = Initializer.initializeTransformedClass("SimilarPrefixClass",prefixClass);
    SootMethod newMethod = new SootMethod("similarPrefix", prefixMethod.getParameterTypes(), prefixMethod.getReturnType(), prefixMethod.getModifiers());
    newMethod.addAllTagsOf(prefixMethod);
    Body newBody = (Body)oldBody.clone();
    newMethod.setActiveBody(newBody);
    newClass.addMethod(newMethod);

    return new Prefix(newMethod, newClass);
  }

}