package org.imdea.fixcheck.assertion;

import org.imdea.fixcheck.prefix.Prefix;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

/**
 * TextDavinci003 class: assertion generator based on the GPT-3.5 model text-davinci-003 (see <a href="https://platform.openai.com/docs/models/gpt-3-5">text-davinci-003</a>)
 *
 * The text-davinci-003 model belongs to the GPT-3.5 family, and they can "can understand and generate natural language or code".
 * Specifically, this model "Can do any language task with better quality, longer output, and consistent instruction-following
 * than the curie, babbage, or ada models. Also supports inserting completions within text.	"
 *
 * @author Facundo Molina
 */
public class TextDavinci003 extends AssertionGenerator {

  private final String MODEL = "text-davinci-003";
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
  public TextDavinci003() {
    maxTokens = 300;
    temperature = 0.5;
  }

  /**
   * Constructor with parameters
   * @param maxTokens Maximum number of tokens to generate in the completion.
   * @param temperature sampling temperature to use.
   */
  public TextDavinci003(int maxTokens, double temperature) {
    this.maxTokens = maxTokens;
    this.temperature = temperature;
  }

  @Override
  public void generateAssertions(Prefix prefix) {
    // Prepare the prompt
    String prompt = generatePrompt(prefix);
    System.out.println("Calling with prompt: " + prompt);
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
  private void performCall() {
    try {
      String prompt = "Say this is a test";
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
      String text = jsonResponse.getJSONArray("choices").getJSONObject(0).getString("text");
      System.out.println(text);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
