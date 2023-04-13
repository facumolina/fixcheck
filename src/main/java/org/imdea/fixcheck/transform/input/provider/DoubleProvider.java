package org.imdea.fixcheck.transform.input.provider;

import java.util.Random;

/**
 * DoubleProvider class: provides doubles for the input transformer.
 * @author Facundo Molina
 */
public class DoubleProvider implements InputProvider {

  private final Random random = new Random();

  @Override
  public Object getInput() {
    return random.nextDouble();
  }

  @Override
  public void addInput(Object value) {
    // Nothing to do
  }

}
