package org.imdea.fixcheck.transform.input.provider;

import java.util.Random;

/**
 * Long Provider class: provides longs for the input transformer.
 * @author Facundo Molina
 */
public class LongProvider implements InputProvider {

  private final Random random = new Random();

  @Override
  public Object getInput() {
    return (long) random.nextInt();
  }

  @Override
  public void addInput(Object value) {
    // Nothing to do
  }

  @Override
  public String toString() {
    return "values: (long) Random.nextInt()";
  }

}
