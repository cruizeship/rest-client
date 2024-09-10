package com.example.restservice;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class FlaskApiClient {

    private static final String FLASK_API_URL = "http://127.0.0.1:5000/embed";

    public static void main(String[] args) {
        try {
            System.out.println("Hello, world!");
            String[] texts = {"Hello, world!", "How are you?"};
            JsonNode response = sendRequest(texts);
            System.out.println("Embeddings: " + response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JsonNode sendRequest(String[] texts) throws IOException {
        // Create HTTP client
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Create POST request
            HttpPost httpPost = new HttpPost(FLASK_API_URL);
            httpPost.setHeader("Content-Type", "application/json");

            // Create JSON payload
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonPayload = objectMapper.writeValueAsString(new RequestPayload(texts));
            StringEntity stringEntity = new StringEntity(jsonPayload);
            httpPost.setEntity(stringEntity);

            // Execute request and get response
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity);

            // Parse JSON response
            ObjectMapper responseMapper = new ObjectMapper();
            return responseMapper.readTree(responseString);
        }
    }

    // Helper class to represent request payload
    private static class RequestPayload {
        public String[] texts;

        public RequestPayload(String[] texts) {
            this.texts = texts;
        }
    }
}
