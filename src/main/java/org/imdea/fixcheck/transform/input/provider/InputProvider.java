package org.imdea.fixcheck.transform.input.provider;

import soot.Value;

/**
 * Input Provider class: provides inputs for the input transformer.
 * @author Facundo Molina
 */
public interface InputProvider {

  /**
   * Get an input value
   * @return an input value
   */
  public Value getInput();

  /**
   * Add an input value
   * @param value - input value
   */
  public void addInput(Value value);

}
