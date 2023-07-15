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

  private static final String specialChars = "!@#$%^&*()_+-=[]{};':,./<>?\\|`~";

  @Override
  public Object getInput() {
    String input = "test";
    if (usedInputs.size() > 0) {
      int i = random.nextInt(usedInputs.size());
      input = (String)usedInputs.get(i);
    }
    // Sometimes, include a special character
    if (random.nextInt(5) == 0 && input.length() > 0) {
      int j = random.nextInt(input.length());
      char randomChar = specialChars.charAt(random.nextInt(specialChars.length()));
      input = input.substring(0, j) + randomChar + input.substring(j);
    }
    return "\""+input+"\"";
  }

  @Override
  public void addInput(Object value) {
    usedInputs.add(value);
  }

  @Override
  public String toString() {
    return "values: " + usedInputs;
  }

}
