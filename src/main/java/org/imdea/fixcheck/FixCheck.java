package org.imdea.fixcheck;

import org.imdea.fixcheck.loader.TestLoader;
import org.imdea.fixcheck.prefix.Prefix;
import org.imdea.fixcheck.runner.PrefixRunner;
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

      for (int i=0; i < n; i++) {
        // Generate n similar prefixes
        System.out.println("Generating similar prefix: " + i);
        PrefixTransformer prefixTransformer = new InputTransformer(prefix);
        Prefix newPrefix = prefixTransformer.transform();

        System.out.println("Generated prefix: " + newPrefix.getMethodClass().getName() + "." + newPrefix.getMethod().getName());
        System.out.println(newPrefix.getMethod().getActiveBody());

        // Run the transformed prefix
        PrefixRunner.runPrefix(newPrefix);

        break;
      }
    }
    return similarPrefixes;
  }

}