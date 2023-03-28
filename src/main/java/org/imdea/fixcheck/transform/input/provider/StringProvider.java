package org.imdea.fixcheck.transform.input.provider;

import soot.Value;
import soot.jimple.StringConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * String Provider class: provides strings for the input transformer.
 */
public class StringProvider implements InputProvider {

  List<Value> usedInputs = new ArrayList<>();
  Random random = new Random();

  public StringProvider() {
    usedInputs.add(StringConstant.v("maxspeed:forward"));
  }

  @Override
  public Value getInput() {
    if (usedInputs.size() > 0) {
      int i = random.nextInt(usedInputs.size());
      return usedInputs.get(i);
    }
    return StringConstant.v("test");
  }

  @Override
  public void addInput(Value value) {
    //usedInputs.add(value);
  }

  @Override
  public String toString() {
    return "values: " + usedInputs;
  }

}
