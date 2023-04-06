package org.imdea.fixcheck.compilation;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;
import java.util.Objects;

/**
 * JavaSourceFromString class: represents a Java source code file.
 * @author Facundo Molina
 */
public class JavaSourceFromString extends SimpleJavaFileObject {

  private String sourceCode;

  public JavaSourceFromString(String name, String sourceCode) {
    super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
    this.sourceCode = Objects.requireNonNull(sourceCode, "sourceCode must not be null");
  }

  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    return sourceCode;
  }

}
