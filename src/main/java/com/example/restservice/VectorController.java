package com.example.restservice;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class VectorController {

    @Autowired
    private JdbcTemplate jdbc;

    @PostMapping("/embedquests")
    public String generateTitleEmbeddings() {
        // Create RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();
        String pythonServiceUrl = "http://localhost:5000/embed";
    
        // Create the headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
    
        // Define the SQL query to select titles where titleEmbedded is false
        String sqlQuery = String.format("SELECT id, description FROM \"%s\".\"Quests\";", "sidequests");
    
        // Use JdbcTemplate's queryForList to capture the extracted data
        List<Map<String, Object>> rows = jdbc.queryForList(sqlQuery);
    
        Map<Integer, String> idToTitleMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Integer questId = (Integer) row.get("id");
            String title = (String) row.get("description");
            idToTitleMap.put(questId, title);
        }
    
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("idTitleMap", idToTitleMap);
    
        // Convert requestBody to JSON string
        String requestBodyJson = new JSONObject(requestBody).toString();
    
        // Send the request to the Python service
        HttpEntity<String> request = new HttpEntity<>(requestBodyJson, headers);
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(pythonServiceUrl, HttpMethod.POST, request, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to connect to Python service.";
        }
    
        try {
            // Process the response from Python service
            Map<Integer, String> embeddings = new HashMap<>();
            JSONObject responseJson = new JSONObject(response.getBody());
            JSONObject idEmbeddingsJson = responseJson.getJSONObject("idEmbeddings");
    
            for (String id : idEmbeddingsJson.keySet()) {
                JSONArray embeddingArray = idEmbeddingsJson.getJSONArray(id);
                Integer questId = Integer.valueOf(id);
                embeddings.put(questId, embeddingArray.toString());
            }
    
            updateQuestEmbeddingsBatch(jdbc, embeddings);
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to parse JSON response from Python service.";
        }
    
        return "Quest embeddings updated successfully!";
    }
    
    @GetMapping("/embedsearch")
    public String searchByText(@RequestParam String searchText) {
        // Create RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();
        String pythonServiceUrl = "http://localhost:5000/embed";

        // Create the headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // Prepare the search text to be sent to the Python API
        Map<String, Object> requestBody = new HashMap<>();
        Map<Integer, String> idTitleMap = new HashMap<>();
        idTitleMap.put(0, searchText);
        requestBody.put("idTitleMap", idTitleMap);  // Only one entry in the list

        // Convert requestBody to JSON string
        String requestBodyJson = new JSONObject(requestBody).toString();

        // Send the request to the Python service
        HttpEntity<String> request = new HttpEntity<>(requestBodyJson, headers);
        System.out.println(request);
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(pythonServiceUrl, HttpMethod.POST, request, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to connect to Python service.";
        }

        try {
            JSONObject responseJson = new JSONObject(response.getBody());
            JSONObject idEmbeddingsJson = responseJson.getJSONObject("idEmbeddings");

            if (idEmbeddingsJson.length() != 1) {
                return "Unexpected number of embeddings received from Python service.";
            }

            // Extract the single search embedding
            String searchId = idEmbeddingsJson.keys().next();
            JSONArray searchEmbedding = idEmbeddingsJson.getJSONArray(searchId);
            String searchEmbeddingString = searchEmbedding.toString();
            System.out.println("Search embedding: " + searchEmbeddingString);
            // Query the PostgreSQL database to get similarity scores using the search embedding
            String sqlQuery = "SELECT title, title_embedding <=> ?::vector(768) AS similarity " +
                            "FROM \"sidequests\".\"Quests\" " +
                            "ORDER BY similarity ASC";

            List<Map<String, Object>> results = jdbc.queryForList(sqlQuery, searchEmbeddingString);

            // Prepare the result string
            StringBuilder result = new StringBuilder("Similarity Scores:\n");

            for (Map<String, Object> row : results) {
                String title = (String) row.get("title");
                Double similarity = (Double) row.get("similarity");
                result.append(String.format("Title: %s, Similarity: %f\n", title, similarity));
            }

            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to parse JSON response from Python service.";
        }
}


public void updateQuestEmbeddingsBatch(JdbcTemplate jdbcTemplate, Map<Integer, String> embeddings) {
    String updateSql = "UPDATE \"sidequests\".\"Quests\" SET title_embedding = ?::vector(768), \"titleEmbedded\" = true WHERE id = ?";

    List<Object[]> batchArgs = new ArrayList<>();
    for (Map.Entry<Integer, String> entry : embeddings.entrySet()) {
        Integer questId = entry.getKey();
        String embeddingString = entry.getValue();
        batchArgs.add(new Object[]{embeddingString, questId});
    }

    jdbcTemplate.batchUpdate(updateSql, batchArgs);
    }

}
