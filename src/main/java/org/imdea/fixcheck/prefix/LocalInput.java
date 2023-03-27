package org.imdea.fixcheck.prefix;

import soot.Local;
import soot.Value;

import java.util.Objects;

/**
 * LocalInput class: represents inputs that are created from local variables.
 * @author Facundo Molina
 */
public class LocalInput extends Input {

  private final Local local; // Soot local

  public LocalInput(String typeName, Local local) {
    super(typeName);
    this.local = Objects.requireNonNull(local, "local cannot be null");
  }

  /**
   * Get the Soot local
   * @return Soot local
   */

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LocalInput)) return false;
    LocalInput that = (LocalInput) o;
    return typeName.equals(that.getType()) && local.equals(that.local);
  }

  @Override
  public Value getValue() { return local; }

  @Override
  public String toString() {
    return "LocalInput: "+local;
  }

}
