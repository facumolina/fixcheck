package org.imdea.fixcheck.assertion;

import org.imdea.fixcheck.prefix.Prefix;

/**
 * AssertionGenerator class: abstract class representing assertion generators for prefixes.
 * @author Facundo Molina
 */
public abstract class AssertionGenerator {

  /**
   * Generate assertions for the prefix.
   */
  public abstract void generateAssertions(Prefix prefix);

}
