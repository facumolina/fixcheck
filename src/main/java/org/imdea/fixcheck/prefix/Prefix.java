package org.imdea.fixcheck.prefix;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import java.util.Objects;

/**
 * Prefix class: represents a prefix, i.e., a sequence of statements revealing a bug addressed by the fix under analysis.
 *
 * @author Facundo Molina
 */
public class Prefix {

  MethodDeclaration method;
  ClassOrInterfaceDeclaration methodClass;
  Prefix parent;

  /**
   * Constructor
   * @param method Soot method of the prefix
   * @param methodClass Soot class of the prefix
   */
  public Prefix(MethodDeclaration method, ClassOrInterfaceDeclaration methodClass) {
    this(method, methodClass, null);
  }

  /**
   * Constructor with a parent prefix (used for prefixes obtained by applying a transformation)
   * @param method Soot method of the prefix
   * @param methodClass Soot class of the prefix
   * @param parent Parent prefix
   */
  public Prefix(MethodDeclaration method, ClassOrInterfaceDeclaration methodClass, Prefix parent) {
    this.method = Objects.requireNonNull(method, "method cannot be null");
    this.methodClass = Objects.requireNonNull(methodClass, "methodClass cannot be null");
    this.parent = parent;
  }

  /**
   * Get the source code of the method
   * @return Source code of the method
   */
  public String getSourceCode() {
    return method.toString();
  }

  /**
   * Get the method
   * @return Soot method of the prefix
   */
  public MethodDeclaration getMethod() { return method; }

  /**
   * Get the method class
   * @return Soot class of the method
   */
  public ClassOrInterfaceDeclaration getMethodClass() { return methodClass; }

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
