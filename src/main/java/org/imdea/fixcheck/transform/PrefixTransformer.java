package org.imdea.fixcheck.transform;

import org.imdea.fixcheck.prefix.Prefix;

import java.util.Objects;

/**
 * PrefixTransformer class: abstract class representing prefix transformers.
 * @author Facundo Molina
 */
public abstract class PrefixTransformer {

  protected Prefix prefix;

  public PrefixTransformer(Prefix prefix) {
    this.prefix = Objects.requireNonNull(prefix, "The prefix cannot be null");
  }

  /**
   * Apply the transformation to the prefix.
   * @return a new transformed prefix.
   */
  public abstract Prefix transform();

}