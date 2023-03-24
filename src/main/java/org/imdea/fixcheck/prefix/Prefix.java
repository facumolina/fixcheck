package org.imdea.fixcheck.prefix;

import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;
import org.imdea.fixcheck.Properties;
import org.imdea.fixcheck.utils.BytecodeUtils;
import soot.SootClass;
import soot.SootMethod;

import java.io.FileNotFoundException;
import java.util.Objects;

/**
 * Prefix class: represents a prefix, i.e., a sequence of statements revealing a bug addressed by the fix under analysis.
 *
 * @author Facundo Molina
 */
public class Prefix {

  SootMethod method;
  SootClass methodClass;
  Prefix parent;

  /**
   * Constructor
   * @param method Soot method of the prefix
   * @param methodClass Soot class of the prefix
   */
  public Prefix(SootMethod method, SootClass methodClass) {
    this(method, methodClass, null);
  }

  /**
   * Constructor with a parent prefix (used for prefixes obtained by applying a transformation)
   * @param method Soot method of the prefix
   * @param methodClass Soot class of the prefix
   * @param parent Parent prefix
   */
  public Prefix(SootMethod method, SootClass methodClass, Prefix parent) {
    this.method = Objects.requireNonNull(method, "method cannot be null");
    this.methodClass = Objects.requireNonNull(methodClass, "methodClass cannot be null");
    this.parent = parent;
  }

  /**
   * Get the source code of the method
   * @return Source code of the method
   */
  public String getSourceCode() {
    if (parent == null) {
      return getSourceCodeForLoadedMethod();
    } else {
      return getSourceCodeForTransformedMethod();
    }
  }

  /**
   * Get the source code of the method
   * @return Source code of the method
   */
  private String getSourceCodeForLoadedMethod() {
    return Properties.TEST_CLASS_SRC
        .findFirst(com.github.javaparser.ast.body.MethodDeclaration.class, md -> md.getNameAsString().equals(method.getName()))
        .orElseThrow(() -> new RuntimeException("Method not found in compilation unit"))
        .toString();
  }

  /**
   * Get the source code of the method after applying a transformation
   * @return Source code of the method
   */
  private String getSourceCodeForTransformedMethod() {
    try {
      Class<?> prefixClass = BytecodeUtils.loadAsClass(methodClass);
      PlainTextOutput plainTextOutput = new PlainTextOutput();
      Decompiler.decompile(prefixClass.getName(), plainTextOutput, DecompilerSettings.javaDefaults());
      return plainTextOutput.toString();
    } catch (FileNotFoundException | ClassNotFoundException e) {
      throw new IllegalStateException("Unable to load the class of a method after applying a transformation: " + methodClass.getName() + method.getName());
    }
  }

  /**
   * Get the method
   * @return Soot method of the prefix
   */
  public SootMethod getMethod() { return method; }

  /**
   * Get the method class
   * @return Soot class of the method
   */
  public SootClass getMethodClass() { return methodClass; }

  /**
   * Get the parent prefix
   * @return Parent prefix
   */
  public Prefix getParent() { return parent; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Prefix)) return false;
    Prefix prefix = (Prefix) o;
    return Objects.equals(method, prefix.method);
  }

  @Override
  public String toString() {
    return "Prefix{" + "method=" + method + '}';
  }

}
