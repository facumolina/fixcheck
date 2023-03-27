package org.imdea.fixcheck.transform.input.provider;

import soot.Value;
import soot.jimple.StringConstant;

/**
 * String Provider class: provides strings for the input transformer.
 */
public class StringProvider implements InputProvider {

  @Override
  public Value getInput() {
    return StringConstant.v("test");
  }

}
