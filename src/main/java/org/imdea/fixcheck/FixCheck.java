package org.imdea.fixcheck;

import org.imdea.fixcheck.loader.TestLoader;
import org.imdea.fixcheck.prefix.Prefix;
import soot.*;
import soot.jimple.JasminClass;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.options.Options;
import soot.util.JasminOutputStream;

import java.io.*;
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

  public static List<Prefix> generateSimilarPrefixes(List<Prefix> prefixes, int n) throws ClassNotFoundException, IOException {
    String inputClass = "java.util.Date";
    String inputType = "java.lang.Object";
    Class<?> inputClassType = Class.forName(inputType);
    List<Prefix> similarPrefixes = new ArrayList<>();
    for (Prefix prefix : prefixes) {
      SootMethod method = prefix.getMethod();
      SootClass sootClass = prefix.getMethodClass();
      Body oldBody = method.retrieveActiveBody();


      for (int i=0; i < n; i++) {
        // Generate n similar prefixes
        SootMethod newMethod = new SootMethod("similarPrefix"+i, method.getParameterTypes(), method.getReturnType(), method.getModifiers());

        sootClass.addMethod(newMethod);
        // Create the method body
        Body newBody = (Body)oldBody.clone();
        newMethod.setActiveBody(newBody);

        for (SootMethod m : sootClass.getMethods()) {
          m.retrieveActiveBody();
        }

        String fileName = SourceLocator.v().getFileNameFor(sootClass, Options.output_format_class);
        File f = new File(fileName);
        f.getParentFile().mkdirs();
        f.createNewFile(); // if file already exists will do nothing
        OutputStream streamOut = new JasminOutputStream(
            new FileOutputStream(f, false));
        PrintWriter writerOut = new PrintWriter(
            new OutputStreamWriter(streamOut));
        JasminClass jasminClass = new soot.jimple.JasminClass(sootClass);
        jasminClass.print(writerOut);
        writerOut.flush();
        streamOut.close();

        break;
      }
    }
    return similarPrefixes;
  }

}