package org.imdea.fixcheck.prefix;

import soot.Local;

import java.util.Objects;

/**
 * LocalInput class: represents inputs that are created from local variables.
 * @author Facundo Molina
 */
public class LocalInput extends Input {

  private final Local local; // Soot local

  public LocalInput(Class<?> type, Local local) {
    super(type);
    this.local = Objects.requireNonNull(local, "local cannot be null");
  }

  /**
   * Get the Soot local
   * @return Soot local
   */
  public Local getLocal() {
    return local;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LocalInput)) return false;
    LocalInput that = (LocalInput) o;
    return type.equals(that.getType()) && local.equals(that.local);
  }

}
