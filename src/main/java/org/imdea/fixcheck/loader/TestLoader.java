package org.imdea.fixcheck.loader;

import java.nio.file.Path;
import java.nio.file.Paths;
import sootup.core.Language;
import sootup.core.Project;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootClassSource;
import sootup.java.core.language.JavaLanguage;

/**
 * TestLoader class: provides methods to load a test class.
 *
 * @author Facundo Molina <facundo.molina@imdea.org>
 */
public class TestLoader {

  private static Project project; // JavaProject based on a input location
  private static View projectView; // View for project, which allows us to retrieve classes

  /**
   * Setup the TestLoader.
   */
  public static void setup(String classesPath) {
    System.out.println("Setting up TestLoader");
    Path pathToBinary = Paths.get(classesPath);
    AnalysisInputLocation<JavaSootClass> inputLocation =
        new PathBasedAnalysisInputLocation(pathToBinary, null);
    Language language = new JavaLanguage(8);
    project = JavaProject.builder((JavaLanguage) language).addInputLocation(inputLocation).build();
    projectView = project.createOnDemandView();
  }

  /**
   * Load a test class.
   *
   * @param testClass is the name of the test class to load
   * @return the loaded test class as a SootClass
   */
  public static SootClass<JavaSootClassSource> loadTestClass(String testClass) {
    System.out.println("Loading test class: " + testClass);
    ClassType classType = project.getIdentifierFactory().getClassType(testClass);
    if (!projectView.getClass(classType).isPresent())
      throw new IllegalArgumentException("The class " + testClass + " is not present in the project. Is the path to the binary correct?");
    return  (SootClass<JavaSootClassSource>) projectView.getClass(classType).get();
  }

}
