package org.imdea.fixcheck.prefix;

import org.imdea.fixcheck.Properties;
import soot.SootClass;
import soot.SootMethod;

import java.util.Objects;

/**
 * Prefix class: represents a prefix, i.e., a sequence of statements revealing a bug addressed by the fix under analysis.
 *
 * @author Facundo Molina
 */
public class Prefix {

  SootMethod method;
  SootClass methodClass;
  String methodSourceCode;
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
    initMethodCode();
  }

  private void initMethodCode() {
    if (parent == null) {
      methodSourceCode = Properties.TEST_CLASS_SRC
          .findFirst(com.github.javaparser.ast.body.MethodDeclaration.class, md -> md.getNameAsString().equals(method.getName()))
          .orElseThrow(() -> new RuntimeException("Method not found in compilation unit"))
          .toString();
    } else {
      methodSourceCode = "Still unable to get the source code of a method after applying a transformation";
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
   * Get the source code of the method
   * @return Source code of the method
   */
  public String getSourceCode() { return methodSourceCode; }

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
