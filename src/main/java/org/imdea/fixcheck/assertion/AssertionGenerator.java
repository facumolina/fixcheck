package org.imdea.fixcheck.assertion;

import org.imdea.fixcheck.prefix.Prefix;
import java.util.Objects;

/**
 * AssertionGenerator class: abstract class representing assertion generators for prefixes.
 * @author Facundo Molina
 */
public abstract class AssertionGenerator {

  protected Prefix prefix; // The prefix to generate assertions for

  public AssertionGenerator(Prefix prefix) {
    this.prefix = Objects.requireNonNull(prefix, "The prefix cannot be null");
  }

  /**
   * Generate assertions for the prefix.
   */
  public abstract void generateAssertions();

}
