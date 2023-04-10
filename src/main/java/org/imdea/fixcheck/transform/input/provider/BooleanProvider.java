package org.imdea.fixcheck.transform.input.provider;

import java.util.Random;

/**
 * Boolean Provider class: provides booleans for the input transformer.
 */
public class BooleanProvider implements InputProvider {

  private final Random random = new Random();

  @Override
  public Object getInput() {
    boolean b = random.nextBoolean();
    return b ? 1 : 0;
  }

  @Override
  public void addInput(Object value) {
    // Nothing to do
  }

  @Override
  public String toString() {
    return "values: [0, 1]";
  }

}
