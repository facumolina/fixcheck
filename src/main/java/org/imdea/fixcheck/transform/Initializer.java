package org.imdea.fixcheck.transform;

import soot.Body;
import soot.SootClass;
import soot.SootMethod;

/**
 * Initializer class: provides initialization methods for transformers.
 * @author Facundo Molina
 */
public class Initializer {

  /**
   * Initialize a transformed class from a soot class.
   * @param newClassName is the name of the new class.
   * @param sootClass is the soot class to initialize the new class from.
   * @return the new transformed class.
   */
  public static SootClass initializeTransformedClass(String newClassName, SootClass sootClass) {
    SootClass transformedClass = new SootClass(newClassName, sootClass.getModifiers(), sootClass.moduleName);
    // Replicate the init method
    SootMethod initMethod = sootClass.getMethodByName("<init>");
    Body initMethodBody = initMethod.retrieveActiveBody();
    SootMethod newInitMethod = new SootMethod("<init>", initMethod.getParameterTypes(), initMethod.getReturnType(), initMethod.getModifiers());
    newInitMethod.addAllTagsOf(initMethod);
    Body newInitMethodBody = (Body)initMethodBody.clone();
    newInitMethod.setActiveBody(newInitMethodBody);
    transformedClass.addMethod(newInitMethod);
    return transformedClass;
  }

}