package org.imdea.fixcheck;

import org.imdea.fixcheck.loader.TestLoader;
import org.imdea.fixcheck.prefix.Prefix;
import org.imdea.fixcheck.transform.Initializer;
import org.imdea.fixcheck.transform.PrefixTransformer;
import org.imdea.fixcheck.transform.input.InputTransformer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import soot.*;
import soot.baf.BafASMBackend;
import soot.jimple.JasminClass;
import soot.options.Options;
import soot.util.JasminOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FixCheck class: main class.
 *
 * @author Facundo Molina
 */
public class FixCheck {
  public static void main(String[] args) {
    System.out.println("> FixCheck");
    String targetTestsPath = args[0];
    String targetTests = args[1];
    int variations = Integer.parseInt(args[2]);
    System.out.println("target tests path: " + targetTestsPath);
    System.out.println("bug revealing tests: " + targetTests);
    System.out.println("variations to analyze: " + variations);
    System.out.println();

    // Loading the prefixes to analyze
    List<Prefix> prefixes = TestLoader.loadPrefixes(targetTestsPath, targetTests);
    System.out.println("prefixes: " + prefixes.size());

    // TODO: analyze the prefixes
    // TODO: first approach: generate similar prefixes by changing the 'inputs' in the given prefixes
    try {
      generateSimilarPrefixes(prefixes, variations);
    } catch (ClassNotFoundException | IOException e) {
      e.printStackTrace();
    }

    System.out.println("Done!");
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
        System.out.println("Generating similar prefix: " + i);
        PrefixTransformer prefixTransformer = new InputTransformer(prefix);
        Prefix newPrefix = prefixTransformer.transform();

        System.out.println("Generated prefix: " + newPrefix.getMethodClass().getName() + "." + newPrefix.getMethod().getName());
        System.out.println(newPrefix.getMethod().getActiveBody());

        // Get the class bytes
        byte[] classBytes = getClassBytes(newPrefix.getMethodClass());

        // Load the class bytes into a new class loader
        ClassLoader loader = new ClassLoader() {
          @Override
          public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (name.equals(newPrefix.getMethodClass().getName())) {
              return defineClass(name, classBytes, 0, classBytes.length);
            }
            return super.loadClass(name);
          }
        };
        Class<?> justCreatedClass = loader.loadClass(newPrefix.getMethodClass().getName());

        System.out.println("Running the test: " + justCreatedClass.getName());
        // Use JUnit core to run the just created test class
        JUnitCore jUnitCore = new JUnitCore();
        Result result = jUnitCore.run(justCreatedClass);
        System.out.printf("Test ran: %s, Failed: %s%n", result.getRunCount(), result.getFailureCount());
        if (result.getFailureCount() > 0) {
          System.out.println("Failures:");
          for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
          }
        }
        break;
      }
    }
    return similarPrefixes;
  }

  public void writeClassFile(SootClass sootClass) throws IOException {
    String fileName = SourceLocator.v().getFileNameFor(sootClass, Options.output_format_class);
    File f = new File(fileName);
    f.getParentFile().mkdirs();
    OutputStream streamOut = new JasminOutputStream(new FileOutputStream(f, false));
    PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
    JasminClass jasminClass = new soot.jimple.JasminClass(sootClass);
    jasminClass.print(writerOut);
    writerOut.flush();
    streamOut.close();
  }

  public static byte[] getClassBytes(SootClass sootClass) {
    int java_version = Options.v().java_version();
    ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
    BafASMBackend backend = new BafASMBackend(sootClass, java_version);
    backend.generateClassFile(streamOut);
    return streamOut.toByteArray();
  }

}