package org.imdea.fixcheck.transform.input.provider;


/**
 * Input Provider class: provides inputs for the input transformer.
 * @author Facundo Molina
 */
public interface InputProvider {

  /**
   * Get an input value
   * @return an input value
   */
  Object getInput();

  /**
   * Add an input value
   * @param value - input value
   */
  void addInput(Object value);

}
