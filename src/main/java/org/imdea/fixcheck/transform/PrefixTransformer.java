package org.imdea.fixcheck.transform;

import org.imdea.fixcheck.prefix.Prefix;

/**
 * PrefixTransformer class: abstract class representing prefix transformers.
 * @author Facundo Molina
 */
public abstract class PrefixTransformer {

  /**
   * Apply the transformation to the prefix.
   * @return a new transformed prefix.
   */
  public abstract Prefix transform(Prefix prefix);

  /**
   * Get the last transformation applied.
   * @return the last transformation applied.
   */
  public abstract String getLastTransformation();
}