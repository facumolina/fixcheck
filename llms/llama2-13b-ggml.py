from llama_cpp import Llama
import flask

llm = Llama(model_path="llms/models/llama-2-13b-chat.ggmlv3.q4_1.bin")

app = flask.Flask(__name__)

@app.route('/complete', methods=['POST'])
def complete():
    prompt = flask.request.json['prompt']
    print("### Prompt:")
    print(prompt)
    output = llm(prompt, max_tokens=50, temperature=0.8, echo=True)
    print("### Model output:")
    print(output)
    return flask.jsonify({'completion': output['choices'][0]['text']})

# Run the app
port = 5100
if __name__ == '__main__':
    app.run(host="0.0.0.0", port=port)
    print("### Listening on port", port)