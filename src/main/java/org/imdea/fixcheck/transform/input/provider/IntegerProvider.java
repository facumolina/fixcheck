package org.imdea.fixcheck.transform.input.provider;

import java.util.Random;

/**
 * Integer Provider class: provides integers for the input transformer.
 * @author Facundo Molina
 */
public class IntegerProvider implements InputProvider {

  private final Random random = new Random();

  @Override
  public Object getInput() {
    return random.nextInt(100);
  }

  @Override
  public void addInput(Object value) {
    // Nothing to do
  }

  @Override
  public String toString() {
    return "values: [0 .. 100]";
  }
}
