package org.imdea.fixcheck;

import org.imdea.fixcheck.loader.TestLoader;
import org.imdea.fixcheck.prefix.Prefix;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.jimple.basic.Local;
import sootup.core.types.PrimitiveType;
import sootup.core.util.Utils;
import sootup.java.core.JavaSootClassSource;
import sootup.java.core.OverridingJavaClassSource;
import sootup.java.core.language.JavaJimple;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * FixCheck class: main class.
 *
 * @author Facundo Molina <facundo.molina@imdea.org>
 */
public class FixCheck {
  public static void main(String[] args) {
    System.out.println("> FixCheck");
    String targetTestsPath = args[0];
    String targetTests = args[1];
    System.out.println("target tests path: " + targetTestsPath);
    System.out.println("bug revealing tests: " + targetTests);
    System.out.println();

    // Loading the prefixes to analyze
    List<Prefix> prefixes = TestLoader.loadPrefixes(targetTestsPath, targetTests);
    System.out.println("prefixes: " + prefixes.size());

    // TODO: analyze the prefixes
    // TODO: first approach: generate similar prefixes by changing the 'inputs' in the given prefixes
  }

  public static List<Prefix> generateSimilarPrefixes(List<Prefix> prefixes, int n) throws ClassNotFoundException {
    String inputClass = "java.util.Date";
    String inputType = "java.lang.Object";
    Class<?> inputClassType = Class.forName(inputType);
    List<Prefix> similarPrefixes = new ArrayList<>();
    for (Prefix prefix : prefixes) {
      SootMethod method = prefix.getMethod();
      SootClass<JavaSootClassSource> sootClass = prefix.getMethodClass();
      Body oldBody = method.getBody();
      for (int i=0; i < n; i++) {
        // Generate n similar prefixes
        // Create Local
        Local newLocal = JavaJimple.newLocal("intVar", PrimitiveType.IntType.getInt());
        // Specify new Method Body
        Body newBody = oldBody.withLocals(Collections.singleton(newLocal));
        // Modify body source
        OverridingBodySource newBodySource = new OverridingBodySource(method.getBodySource()).withBody(newBody);
        // Create OverridingClassSource
        OverridingJavaClassSource overridingJavaClassSource =
            new OverridingJavaClassSource(sootClass.getClassSource());
        // Create new Method
        SootMethod newMethod = method.withOverridingMethodSource(old -> newBodySource);
        OverridingJavaClassSource newClassSource = overridingJavaClassSource.withMethods(Collections.singleton(newMethod));;
        SootClass<JavaSootClassSource> newClass = sootClass.withClassSource(newClassSource);
        System.out.println("new class");
        for (SootMethod m : newClass.getMethods()) {
          System.out.println(m.getBody());
        }
        Utils.outputJimple(newClass, true);
        break;
      }
    }
    return similarPrefixes;
  }

}