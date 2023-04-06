package org.imdea.fixcheck.compilation;

import javax.tools.*;
import java.util.Hashtable;
import java.util.Map;

/**
 * InMemoryFileManager class: represents a file manager that stores the compiled classes in memory.
 * @author Facundo Molina
 */
public class InMemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {

  private Map<String, JavaClassAsBytes> compiledClasses;
  private ClassLoader loader;

  public InMemoryFileManager(StandardJavaFileManager standardManager) {
    super(standardManager);
    this.compiledClasses = new Hashtable<>();
    this.loader = new InMemoryClassLoader(this.getClass().getClassLoader(), this);
  }

  @Override
  public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
    JavaClassAsBytes classAsBytes = new JavaClassAsBytes(className, kind);
    compiledClasses.put(className, classAsBytes);
    return classAsBytes;
  }

  public Map<String, JavaClassAsBytes> getBytesMap() {
    return compiledClasses;
  }

  @Override
  public ClassLoader getClassLoader(Location location) {
    return loader;
  }

}
