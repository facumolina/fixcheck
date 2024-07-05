package org.imdea.fixcheck.assertion;

import com.github.javaparser.ast.CompilationUnit;
import de.kherud.llama.InferenceParameters;
import de.kherud.llama.LlamaModel;
import de.kherud.llama.LlamaOutput;
import de.kherud.llama.ModelParameters;
import org.imdea.fixcheck.assertion.common.AssertionsHelper;
import org.imdea.fixcheck.prefix.Prefix;
import org.imdea.fixcheck.transform.common.TransformationHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * CodeLlama class: assertion generator based on the CodeLlama model by Meta.
 *
 * @author Facundo Molina <facundo.molina@imdea.org>
 */
public class CodeLlamaInstructJavaBindings extends AssertionGenerator {

  private final String model_gguf = "llms/models/codellama-7b-instruct.Q5_K_M.gguf";
  private final LlamaModel codeLlamaModel;

  public CodeLlamaInstructJavaBindings() {
    LlamaModel.setLogger(null, (level, message) -> {});
    String projectDir = System.getProperty("user.dir");
    ModelParameters modelParams = new ModelParameters().setModelFilePath(projectDir + "/" + model_gguf);
    codeLlamaModel = new LlamaModel(modelParams);
  }

  @Override
  public void generateAssertions(Prefix prefix) {
    // Prepare the prompt
    String prompt = generatePrompt(prefix);
    System.out.println("prompt:");
    System.out.println(prompt);

    // Calling the model
    String responseText = performCall(prompt);

    // Processing output
    List<String> assertionsStr = getAssertionsFromResponseText(responseText);
    System.out.println("---> assertions: " + assertionsStr);
    System.out.println();
    AssertionsHelper.appendAssertionsToPrefix(assertionsStr, prefix);
    // Update the class name
    updateClassName(prefix);
  }

  /**
   * Generate the prompt for the model. The prompt will have the following format:
   * 'Given the following Java test case:'
   *   <test case code>
   * 'Produce as output assertions for the following test case:'
   *   <prefix code>
   *
   * @param prefix Prefix to generate the prompt for
   * @return Prompt for the model
   */
  private String generatePrompt(Prefix prefix) {
    String prompt = "Complete the second Java unit test with assertions:\n";
    prompt += prefix.getParent().getSourceCode() + "\n";
    prompt += prefix.getSourceCode();
    // remove the last } so that the model can complete the code
    prompt = replaceLast(prompt, "}", "");
    return prompt;
  }

  private String replaceLast(String string, String substring, String replacement) {
    int index = string.lastIndexOf(substring);
    if (index == -1)
      return string;
    return string.substring(0, index) + replacement
        + string.substring(index+substring.length());
  }

  /**
   * Perform the call to the OpenAI API.
   */
  private String performCall(String prompt) {
    InferenceParameters inferParams = new InferenceParameters(prompt)
        .setTemperature(0.8f)
        .setPenalizeNl(true)
        .setStopStrings("}");
    String completion = "";
    for (LlamaOutput output : codeLlamaModel.generate(inferParams)) {
      System.out.print(output);
      completion += output;
    }
    System.out.println("---> response: " + completion);
    return completion;
  }

  /**
   * Get assertions as strings from response text
   * @param text the response
   * @return a list of assertion strings
   */
  private List<String> getAssertionsFromResponseText(String text) {
    List<String> assertionsStr = new ArrayList<>();
    String[] lines = text.split("\\r?\\n"); // Split by lines
    // Process the lines of Strings backwards, until the first assertion is found
    boolean withinAssertions = false;
    for (int i = lines.length - 1; i >= 0; i--) {
      String line = lines[i];
      if (isAssertionString(line)) {
        withinAssertions = true;
        assertionsStr.add(line);
      } else if (withinAssertions) {
        break;
      }
    }
    return assertionsStr;
  }

  private boolean isAssertionString(String possibleAssertion) {
    return possibleAssertion.contains("assertEquals") ||
        possibleAssertion.contains("assertNotNull") ||
        possibleAssertion.contains("assertNull") ||
        possibleAssertion.contains("assertTrue") ||
        possibleAssertion.contains("assertFalse");
  }

  /**
   * Update the class name with a new name
   * @param prefix Prefix to update
   */
  private void updateClassName(Prefix prefix) {
    String currentClassName = prefix.getClassName();
    String newClassName = currentClassName + "withCodeLlamaInstructJavaBindings";
    prefix.setClassName(newClassName);
    CompilationUnit compilationUnit = prefix.getMethodCompilationUnit();
    compilationUnit.getClassByName(currentClassName).get().setName(newClassName);
    TransformationHelper.updateCompilationUnitNames(compilationUnit, currentClassName, newClassName);
  }

}

