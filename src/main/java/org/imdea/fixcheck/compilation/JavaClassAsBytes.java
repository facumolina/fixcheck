package org.imdea.fixcheck.compilation;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * JavaClassAsBytes class: represents a Java class as bytes.
 */
public class JavaClassAsBytes extends SimpleJavaFileObject {

  protected ByteArrayOutputStream bos = new ByteArrayOutputStream();

  public JavaClassAsBytes(String name, Kind kind) {
    super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
  }

  public byte[] getBytes() {
    return bos.toByteArray();
  }

  @Override
  public OutputStream openOutputStream() {
    return bos;
  }
}
