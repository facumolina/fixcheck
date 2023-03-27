package org.imdea.fixcheck.prefix;

import soot.Value;

/**
 * ConstantInput class: represents inputs that are created from constants.
 * @author Facundo Molina
 */
public class ConstantInput extends Input {

  private final Value constant;

  public ConstantInput(String typeName, Value constant) {
    super(typeName);
    this.constant = constant;
  }

  @Override
  public Value getValue() { return constant; }

  @Override
  public String toString() {
    return "ConstantInput: "+constant;
  }

}
