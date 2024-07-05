from transformers import AutoModelForCausalLM, AutoTokenizer
import flask

tokenizer = AutoTokenizer.from_pretrained('replit/replit-code-v1-3b', trust_remote_code=True)
model = AutoModelForCausalLM.from_pretrained('replit/replit-code-v1-3b', trust_remote_code=True)

# Run flask server
app = flask.Flask(__name__)

@app.route('/complete', methods=['POST'])
def complete():
    prompt = flask.request.json['prompt']
    print("### Prompt:")
    print(prompt)
    x = tokenizer.encode(prompt, return_tensors='pt')
    total_length = len(x[0])
    max_l = total_length + 48
    y = model.generate(x, max_length=max_l, temperature=0.2, top_p=0.9, top_k=4, num_return_sequences=1, eos_token_id=tokenizer.eos_token_id)
    print("### Model output:")
    output = tokenizer.decode(y[0], skip_special_tokens=True, clean_up_tokenization_spaces=False)
    without_prompt = output.replace(prompt, '')
    print(output)
    return flask.jsonify({'completion': output})

# Run the app
port = 5100
if __name__ == '__main__':
    app.run(host="0.0.0.0", port=port)
    print("### Listening on port", port)