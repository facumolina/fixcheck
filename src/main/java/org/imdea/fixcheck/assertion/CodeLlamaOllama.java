package org.imdea.fixcheck.assertion;

import com.github.javaparser.ast.CompilationUnit;
import org.imdea.fixcheck.assertion.common.AssertionsHelper;
import org.imdea.fixcheck.prefix.Prefix;
import org.imdea.fixcheck.transform.common.TransformationHelper;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Llama3_1Ollama class: assertion generator using the Llama3.1 model through Ollama.
 *
 * @author Facundo Molina <facundo.molina@imdea.org>
 */
public class CodeLlamaOllama extends AssertionGenerator {

  private final String API_URL = "http://localhost:11434/api/generate";

  private final String SYSTEM = "You are an expert programmer that helps complete Java unit tests with test assertions. Don't explain anything just write the tests.";
  public CodeLlamaOllama() {}

  @Override
  public void generateAssertions(Prefix prefix) {
    // Prepare the prompt
    String prompt = generatePrompt(prefix);
    System.out.println("prompt:");
    System.out.println(prompt);
    // Perform the call to the OpenAI API
    String responseText = performCall(prompt);
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
    String prompt = "You are an expert programmer that helps complete Java unit tests with test assertions. " +
        "Avoid using text. Don't explain anything just complete the given code snippet with the " +
        "corresponding test assertions";
    prompt += prefix.getParent().getSourceCode() + "\n";
    //prompt += "Problem: produce the assertions for the following test:\n";
    prompt += prefix.getSourceCode();
    //prompt += "[/INST]";
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
    try {
      URL url = new URL(API_URL);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("POST");
      con.setRequestProperty("Content-Type", "application/json");
      JSONObject requestBody = new JSONObject();
      requestBody.put("model", "codellama");
      requestBody.put("system", SYSTEM);
      requestBody.put("prompt", prompt);
      //requestBody.put("options", new JSONObject().put("stop", new JSONArray().put("\n\n")));
      //requestBody.put("num_predict", 48);
      requestBody.put("stream", false);
      System.out.println("request: " + requestBody);
      con.setDoOutput(true);
      con.getOutputStream().write(requestBody.toString().getBytes("UTF-8"));
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();
      JSONObject jsonResponse = new JSONObject(response.toString());
      String completion = jsonResponse.getString("response");
      System.out.println("---> response: " + completion);
      return completion;
    } catch (Exception e) {
      System.out.println("Error while performing the call to the model llama3.1 through Ollama");
      e.printStackTrace();
      throw new RuntimeException(e);
    }
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
    String newClassName = currentClassName + "withCodeLlama";
    prefix.setClassName(newClassName);
    CompilationUnit compilationUnit = prefix.getMethodCompilationUnit();
    compilationUnit.getClassByName(currentClassName).get().setName(newClassName);
    TransformationHelper.updateCompilationUnitNames(compilationUnit, currentClassName, newClassName);
  }
}
