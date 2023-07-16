import gpt4all
import flask

prompt = """
          public void testWithScalar118() throws Exception
              {
                  ObjectMapper mapper = new ObjectMapper();
                  String json;

                  ExternalTypeWithNonPOJO input = new ExternalTypeWithNonPOJO(new java.util.Date(123L));
                  json = mapper.writeValueAsString(input);
                  System.out.println("JSON with Date: "+json);
                  assertNotNull(json);

                  // and back just to be sure:
                  ExternalTypeWithNonPOJO result = mapper.readValue(json, ExternalTypeWithNonPOJO.class);
                  assertNotNull(result.value);
                  assertTrue(result.value instanceof java.util.Date);
              }

          public void testWithNaturalScalar118() throws Exception
              {
                  ExternalTypeWithNonPOJO input = new ExternalTypeWithNonPOJO(Integer.valueOf(13));
                  String json = MAPPER.writeValueAsString(input);
                  // and back just to be sure:
                  ExternalTypeWithNonPOJO result = MAPPER.readValue(json, ExternalTypeWithNonPOJO.class);
           """

model = gpt4all.GPT4All("ggml-replit-code-v1-3b")

#print("### Prompt:")
#prompt = "### Human:\nWhat is the meaning of life\n### Assistant:"
#print(prompt)
#print()

#print("### Model output:")
# generate(prompt, max_tokens=200, temp=0.7, top_k=40, top_p=0.1, repeat_penalty=1.18, repeat_last_n=64, n_batch=8, n_predict=None, streaming=False)

#output = model.generate(prompt, max_tokens=48, temp=0.2, repeat_penalty=1, top_p=0.9, top_k=4)
#print(output)

# Run flask server
app = flask.Flask(__name__)

@app.route('/complete', methods=['POST'])
def complete():
    prompt = flask.request.json['prompt']
    print("### Prompt:")
    print(prompt)
    output = model.generate(prompt, max_tokens=48, temp=0.2, repeat_penalty=1, top_p=0.9, top_k=4)
    print("### Model output:")
    print(output)
    return flask.jsonify({'completion': output})

# Run the app
port = 5100
if __name__ == '__main__':
    app.run(host="0.0.0.0", port=port)