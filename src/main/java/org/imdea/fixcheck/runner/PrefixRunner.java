package org.imdea.fixcheck.runner;

import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;
import org.imdea.fixcheck.prefix.Prefix;
import org.imdea.fixcheck.utils.BytecodeUtils;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.*;

/**
 * PrefixRunner class: provide methods to run a prefix.
 * @author Facundo Molina
 */
public class PrefixRunner {

  public static Result runPrefix(Prefix prefix) throws ClassNotFoundException, FileNotFoundException {
    // Load as class
    /*Class<?> justCreatedClass = BytecodeUtils.loadAsClass(prefix.getMethodClass());
    // Decompile and print
    PlainTextOutput plainTextOutput = new PlainTextOutput();
    Decompiler.decompile(justCreatedClass.getName(), plainTextOutput, DecompilerSettings.javaDefaults());
    System.out.println("---> decompiled prefix: ");
    System.out.println();
    System.out.println(plainTextOutput);
    System.out.println("---> running test: " + justCreatedClass.getName());
    // Use JUnit core to run the just created test class
    JUnitCore jUnitCore = new JUnitCore();
    Result result = jUnitCore.run(justCreatedClass);
    System.out.printf("---> test ran: %s, Failed: %s%n", result.getRunCount(), result.getFailureCount());
    if (result.getFailureCount() > 0) {
      System.out.println("---> failures:");
      for (Failure failure : result.getFailures()) {
        System.out.println("\t"+failure.toString());
      }
    }
    return result;*/
    return null;
  }

}
