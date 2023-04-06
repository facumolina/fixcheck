package org.imdea.fixcheck.prefix;

import soot.Value;

import java.util.Objects;

/**
 * Input class: represents an input for a prefix.
 * @author Facundo Molina
 */
public abstract class Input {

  protected String typeName;

  /**
   * Constructor
   * @param typeName Type of the input
   */
  public Input(String typeName) {
    this.typeName = Objects.requireNonNull(typeName, "typeName cannot be null");
  }

  /**
   * Get the type of the input
   * @return Type of the input
   */
  public String getType() {
    return typeName;
  }

  /**
   * Get the value of the input
   * @return Value of the input
   */
  public abstract Value getValue();

}
