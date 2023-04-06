package org.imdea.fixcheck.compilation;

import java.util.Map;
import java.util.Objects;

/**
 * InMemoryClassLoader class: represents a class loader that loads classes from memory.
 */
public class InMemoryClassLoader extends ClassLoader {

  private InMemoryFileManager manager;

  public InMemoryClassLoader(ClassLoader parent, InMemoryFileManager manager) {
    super(parent);
    this.manager = Objects.requireNonNull(manager, "manager must not be null");
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {

    Map<String, JavaClassAsBytes> compiledClasses = manager.getBytesMap();

    if (compiledClasses.containsKey(name)) {
      byte[] bytes = compiledClasses.get(name).getBytes();
      return defineClass(name, bytes, 0, bytes.length);
    } else {
      throw new ClassNotFoundException();
    }
  }
}
