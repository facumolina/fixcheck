package org.imdea.fixcheck.assertion;

import com.github.javaparser.ast.CompilationUnit;
import org.imdea.fixcheck.assertion.common.AssertionsHelper;
import org.imdea.fixcheck.prefix.Prefix;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.imdea.fixcheck.transform.common.TransformationHelper;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * GPT-3.5-turbo class: assertion generator based on the GPT-3.5-turbo model
 * (see <a href="https://platform.openai.com/docs/models/gpt-3-5-turbo">gpt-3.5-turbo</a>)
 *
 * @author Facundo Molina <facundo.molina@imdea.org>
 */
public class GPT3_5Turbo extends AssertionGenerator {

  private final String MODEL = "gpt-3.5-turbo";
  private final String API_URL = "https://api.openai.com/v1/completions";

  private final String API_KEY = System.getenv("OPENAI_API_KEY");

  // The maximum number of tokens to generate in the completion, defaults to 16
  private int maxTokens;

  // What sampling temperature to use, between 0 and 2.
  // Higher values like 0.8 will make the output more random, while lower values like 0.2 will
  // make it more focused and deterministic.
  private double temperature;

  /**
   * Default constructor
   */
  public GPT3_5Turbo() {
    maxTokens = 500;
    temperature = 0.5;
  }

  /**
   * Constructor with parameters
   * @param maxTokens Maximum number of tokens to generate in the completion.
   * @param temperature sampling temperature to use.
   */
  public GPT3_5Turbo(int maxTokens, double temperature) {
    this.maxTokens = maxTokens;
    this.temperature = temperature;
  }

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
    String prompt = "Given the following Java test case:\n";
    prompt += prefix.getParent().getSourceCode() + "\n";
    prompt += "Produce as output assertions for the following test case:\n";
    prompt += prefix.getSourceCode();
    return prompt;
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
      con.setRequestProperty("Authorization", "Bearer " + API_KEY);
      JSONObject requestBody = new JSONObject();
      requestBody.put("model", MODEL);
      requestBody.put("prompt", prompt);
      requestBody.put("max_tokens", maxTokens);
      requestBody.put("temperature", temperature);
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
      JSONArray choices = jsonResponse.getJSONArray("choices");
      System.out.println("---> response: " + choices);
      return choices.getJSONObject(0).getString("text"); // Return the first choice
    } catch (Exception e) {
      System.out.println("Error while performing the call to the OpenAI API");
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
    for (String possibleAssertion : lines) {
      if (possibleAssertion.startsWith("//")) continue; // It's a comment
      if (possibleAssertion.trim().isEmpty()) continue; // It's an empty line
      assertionsStr.add(possibleAssertion);
    }
    return assertionsStr;
  }

  /**
   * Valid/fix the given assertion string
   */
  private String validateOrFixAssertionStr(String assertionStr) { return ""; }

  /**
   * Update the class name with a new name
   * @param prefix Prefix to update
   */
  private void updateClassName(Prefix prefix) {
    String currentClassName = prefix.getClassName();
    String newClassName = currentClassName + "withTextDavinci003";
    prefix.setClassName(newClassName);
    CompilationUnit compilationUnit = prefix.getMethodCompilationUnit();
    compilationUnit.getClassByName(currentClassName).get().setName(newClassName);
    TransformationHelper.updateCompilationUnitNames(compilationUnit, currentClassName, newClassName);
  }

}