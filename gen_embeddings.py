from flask import Flask, request, jsonify
from flask_cors import CORS  # Import CORS
from transformers import AutoTokenizer, AutoModel
import torch

app = Flask(__name__)
CORS(app)  # Enable CORS for all domains

tokenizer = AutoTokenizer.from_pretrained('distilbert-base-uncased')
model = AutoModel.from_pretrained('distilbert-base-uncased')

@app.route('/embed', methods=['POST'])
def embed():
    data = request.json
    print(data)
    # Extract the idTitleMap from the request
    id_title_map = data.get('idTitleMap', {})
    
    # Extract the titles from the idTitleMap
    texts = list(id_title_map.values())

    # Tokenize the titles
    inputs = tokenizer(texts, return_tensors='pt', padding=True, truncation=True)
    
    # Generate embeddings
    with torch.no_grad():
        outputs = model(**inputs)
    
    # Calculate the mean of the hidden states for each text
    embeddings = outputs.last_hidden_state.mean(dim=1).numpy().tolist()
    
    # Create a response mapping IDs to their embeddings
    id_embeddings = {id_: embedding for id_, embedding in zip(id_title_map.keys(), embeddings)}
    
    return jsonify({'idEmbeddings': id_embeddings})

if __name__ == '__main__':
    app.run(port=5000)
