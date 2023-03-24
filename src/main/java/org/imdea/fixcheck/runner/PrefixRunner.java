package org.imdea.fixcheck.runner;

import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;
import org.imdea.fixcheck.Properties;
import org.imdea.fixcheck.prefix.Prefix;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import soot.SootClass;
import soot.SourceLocator;
import soot.baf.BafASMBackend;
import soot.jimple.JasminClass;
import soot.options.Options;
import soot.util.JasminOutputStream;

import java.io.*;

/**
 * PrefixRunner class: provide methods to run a prefix.
 * @author Facundo Molina
 */
public class PrefixRunner {

  public static void runPrefix(Prefix prefix) throws ClassNotFoundException, FileNotFoundException {
    // Get the class bytes
    byte[] classBytes = getClassBytesAndSaveToFile(prefix.getMethodClass());

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

  private static byte[] getClassBytesAndSaveToFile(SootClass sootClass) throws FileNotFoundException {
    String fileName = SourceLocator.v().getFileNameFor(sootClass, Options.output_format_class);
    fileName = fileName.replace("sootOutput", Properties.TEST_CLASSES_PATH);
    File f = new File(fileName);
    f.getParentFile().mkdirs();
    OutputStream streamOut = new JasminOutputStream(new FileOutputStream(f, false));
    PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
    JasminClass jasminClass = new soot.jimple.JasminClass(sootClass);
    jasminClass.print(writerOut);
    writerOut.flush();
    writerOut.close();
    return getClassBytes(sootClass);
  }

}
