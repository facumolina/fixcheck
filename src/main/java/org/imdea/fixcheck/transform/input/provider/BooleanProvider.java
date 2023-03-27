package org.imdea.fixcheck.transform.input.provider;

import soot.Value;
import soot.jimple.IntConstant;

import java.util.Random;

/**
 * Boolean Provider class: provides booleans for the input transformer.
 */
public class BooleanProvider implements InputProvider {

  private final Random random = new Random();

  @Override
  public Value getInput() {
    boolean b = random.nextBoolean();
    return IntConstant.v(b ? 1 : 0);
  }

}
