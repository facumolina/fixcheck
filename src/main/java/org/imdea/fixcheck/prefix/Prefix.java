package org.imdea.fixcheck.prefix;

import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.java.core.JavaSootClassSource;

import java.util.Objects;

/**
 * Prefix class: represents a prefix, i.e., a sequence of statements revealing a bug addressed by the fix under analysis.
 *
 * @author Facundo Molina <facundo.molina@imdea.org>
 */
public class Prefix {

  SootMethod method;
  SootClass<JavaSootClassSource> methodClass;

  public Prefix(SootMethod method, SootClass<JavaSootClassSource> methodClass) {
    this.method = Objects.requireNonNull(method, "method cannot be null");
    this.methodClass = Objects.requireNonNull(methodClass, "methodClass cannot be null");
  }

  public SootMethod getMethod() {
    return method;
  }

  public SootClass<JavaSootClassSource> getMethodClass() {
    return methodClass;
  }

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
