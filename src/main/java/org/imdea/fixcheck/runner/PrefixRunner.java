package org.imdea.fixcheck.runner;

import org.imdea.fixcheck.Properties;
import org.imdea.fixcheck.compilation.InMemoryFileManager;
import org.imdea.fixcheck.compilation.JavaSourceFromString;
import org.imdea.fixcheck.prefix.Prefix;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * PrefixRunner class: provide methods to run a prefix.
 * @author Facundo Molina
 */
public class PrefixRunner {

  /**
   * Run a prefix.
   * @param prefix Prefix to run
   * @return Result of the execution
   */
  public static Result runPrefix(Prefix prefix) throws ClassNotFoundException, IOException {
    System.out.println("---> prefix: ");
    System.out.println(prefix.getMethod());

    System.out.println("---> going to compile test class: " + prefix.getClassName());

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    InMemoryFileManager manager = new InMemoryFileManager(compiler.getStandardFileManager(null, null, null));


    String sourceCode = prefix.getClassSourceCode();

    List<JavaFileObject> sourceFiles = Collections.singletonList(new JavaSourceFromString(prefix.getClassName(), sourceCode));

    // set compiler's classpath to the full classpath
    List<String> optionList = new ArrayList<>(Arrays.asList("-classpath", Properties.FULL_CLASSPATH));

    JavaCompiler.CompilationTask task = compiler.getTask(null, manager, diagnostics, optionList, null, sourceFiles);

    boolean result = task.call();

    if (!result) {
      diagnostics.getDiagnostics().forEach(System.out::println);
    } else {
      ClassLoader classLoader = manager.getClassLoader(null);
      Class<?> justCreatedClass = classLoader.loadClass(prefix.getFullClassName());
      System.out.println("---> running test class: " + justCreatedClass.getName());

      // Use JUnit core to run the prefix test
      JUnitCore jUnitCore = new JUnitCore();
      Description description = Description.createTestDescription(justCreatedClass, prefix.getMethod().getNameAsString());
      Request req = Request.aClass(justCreatedClass).filterWith(description);
      System.out.println("---> description: " + description);
      Result testResult = jUnitCore.run(req);
      System.out.printf("---> test ran: %s, Failed: %s%n", testResult.getRunCount(), testResult.getFailureCount());
      if (testResult.getFailureCount() > 0) {
        System.out.println("---> failures:");
        for (Failure failure : testResult.getFailures()) {
          System.out.println("\t"+failure.toString());
        }
      }
      return testResult;

    }

    throw new IllegalStateException("Compilation failed, unable to run test");
  }

}
