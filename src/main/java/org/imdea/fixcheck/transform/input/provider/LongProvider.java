package org.imdea.fixcheck.transform.input.provider;

import soot.Value;
import soot.jimple.LongConstant;
import java.util.Random;

/**
 * Long Provider class: provides longs for the input transformer.
 * @author Facundo Molina
 */
public class LongProvider implements InputProvider {

  private final Random random = new Random();

  @Override
  public Value getInput() {
    return LongConstant.v(random.nextLong());
  }
}
