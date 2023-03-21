package org.imdea.fixcheck;

/**
 * Properties class: contains properties to configure the FixCheck execution.
 * @author Facundo Molina
 */
public class Properties {

  public static String TEST_CLASSES_PATH; // Full path to the test classes + dependencies + fixcheck
  public static String TEST_CLASS; // Full name of the test class
  public static String TARGET_CLASS; // Full name of the target class
  public static int PREFIX_VARIATIONS = 1; // Total number of prefix variations to generate

}
