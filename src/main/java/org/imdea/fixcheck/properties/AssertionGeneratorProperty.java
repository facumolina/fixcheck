package org.imdea.fixcheck.properties;

import java.util.HashMap;
import java.util.Map;

/**
 * AssertionGeneratorOptions class: handle the options for the assertion generator.
 * @author Facundo Molina
 */
public class AssertionGeneratorProperty {

  // Declare and initialize the static map
  private static final Map<String, String> Options = createMap();

  // Method to initialize the map with values
  private static Map<String, String> createMap() {
    Map<String, String> map = new HashMap<>();
    map.put("assert-true", "org.imdea.fixcheck.assertion.AssertTrueGenerator");
    map.put("previous-assertion", "org.imdea.fixcheck.assertion.UsePreviousAssertGenerator");
    map.put("replit-code-llm", "org.imdea.fixcheck.assertion.ReplitCodeLLM");
    map.put("gpt-3.5", "org.imdea.fixcheck.assertion.GPT35Turbo");
    map.put("ollama", "org.imdea.fixcheck.assertion.OllamaAssertionGenerator");
    return map;
  }

  public static String parseOption(String option) {
    if (!Options.containsKey(option)) {
      System.out.println("Error: Invalid option for assertion generator: " + option);
      System.out.println("Valid options are: " + Options.keySet());
      System.exit(1);
    }
    return Options.get(option);
  }
}
