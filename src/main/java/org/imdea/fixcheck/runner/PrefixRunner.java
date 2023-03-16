package org.imdea.fixcheck.runner;

import org.imdea.fixcheck.prefix.Prefix;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import soot.SootClass;
import soot.baf.BafASMBackend;
import soot.options.Options;

import java.io.ByteArrayOutputStream;

/**
 * PrefixRunner class: provide methods to run a prefix.
 * @author Facundo Molina
 */
public class PrefixRunner {

  public static void runPrefix(Prefix prefix) throws ClassNotFoundException {
    // Get the class bytes
    byte[] classBytes = getClassBytes(prefix.getMethodClass());

    // Load the class bytes into a new class loader
    ClassLoader loader = new ClassLoader() {
      @Override
      public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.equals(prefix.getMethodClass().getName())) {
          return defineClass(name, classBytes, 0, classBytes.length);
        }
        return super.loadClass(name);
      }
    };
    Class<?> justCreatedClass = loader.loadClass(prefix.getMethodClass().getName());

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
  }

  /**
   * Get the class bytes from a SootClass.
   * @param sootClass the SootClass
   * @return the class bytes
   */
  private static byte[] getClassBytes(SootClass sootClass) {
    int java_version = Options.v().java_version();
    ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
    BafASMBackend backend = new BafASMBackend(sootClass, java_version);
    backend.generateClassFile(streamOut);
    return streamOut.toByteArray();
  }

}
