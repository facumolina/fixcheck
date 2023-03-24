package org.imdea.fixcheck.utils;

import org.imdea.fixcheck.Properties;
import soot.SootClass;
import soot.SourceLocator;
import soot.baf.BafASMBackend;
import soot.jimple.JasminClass;
import soot.options.Options;
import soot.util.JasminOutputStream;

import java.io.*;

/**
 * BytecodeUtils class: contains utility methods to work with bytecode.
 * @author Facundo Molina
 */
public class BytecodeUtils {

  /**
   * Load a SootClass as a Class<?> object.
   * @param sootClass the SootClass
   * @return the corresponding class object.
   */
  public static Class<?> loadAsClass(SootClass sootClass) throws FileNotFoundException, ClassNotFoundException {
    // Get the class bytes
    byte[] classBytes = BytecodeUtils.getClassBytecodeAndSaveToFile(sootClass);

    // Load the class bytes into a new class loader
    ClassLoader loader = new ClassLoader() {
      @Override
      public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.equals(sootClass.getName())) {
          return defineClass(name, classBytes, 0, classBytes.length);
        }
        return super.loadClass(name);
      }
    };
    return loader.loadClass(sootClass.getName());
  }

  /**
   * Get the class bytecode as byte[] from a SootClass.
   * @param sootClass the SootClass
   * @return the class bytecode
   */
  public static byte[] getClassBytecode(SootClass sootClass) {
    int java_version = Options.v().java_version();
    ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
    BafASMBackend backend = new BafASMBackend(sootClass, java_version);
    backend.generateClassFile(streamOut);
    return streamOut.toByteArray();
  }

  /**
   * Get the class bytecode as byte[] from a SootClass and save it to a file.
   * @param sootClass the SootClass
   * @return the class bytecode
   * @throws FileNotFoundException if the file cannot be created
   */
  public static byte[] getClassBytecodeAndSaveToFile(SootClass sootClass) throws FileNotFoundException {
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
    return getClassBytecode(sootClass);
  }

}
