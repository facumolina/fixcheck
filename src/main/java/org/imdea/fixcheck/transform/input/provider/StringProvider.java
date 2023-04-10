package org.imdea.fixcheck.transform.input.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * String Provider class: provides strings for the input transformer.
 */
public class StringProvider implements InputProvider {

  List<Object> usedInputs = new ArrayList<>();
  Random random = new Random();

  @Override
  public Object getInput() {
    if (usedInputs.size() > 0) {
      int i = random.nextInt(usedInputs.size());
      return usedInputs.get(i);
    }
    return "\"test\"";
  }

  @Override
  public void addInput(Object value) {
    usedInputs.add("\""+value+"\"");
  }

  @Override
  public String toString() {
    return "values: " + usedInputs;
  }

}
