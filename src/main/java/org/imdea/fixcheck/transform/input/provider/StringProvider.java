package org.imdea.fixcheck.transform.input.provider;

import soot.Value;
import soot.jimple.StringConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * String Provider class: provides strings for the input transformer.
 */
public class StringProvider implements InputProvider {

  List<Value> usedInputs = new ArrayList<>();

  @Override
  public Value getInput() {
    return StringConstant.v("test");
  }

  @Override
  public void addInput(Value value) {
    usedInputs.add(value);
  }

  @Override
  public String toString() {
    return "values: " + usedInputs;
  }

}
