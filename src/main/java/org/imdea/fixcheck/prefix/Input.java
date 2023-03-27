package org.imdea.fixcheck.prefix;

import java.util.Objects;

/**
 * Input class: represents an input for a prefix.
 * @author Facundo Molina
 */
public abstract class Input {

  protected Class<?> type;

  /**
   * Constructor
   * @param type Type of the input
   */
  public Input(Class<?> type) {
    this.type = Objects.requireNonNull(type, "type cannot be null");
  }

  /**
   * Get the type of the input
   * @return Type of the input
   */
  public Class<?> getType() {
    return type;
  }

}
