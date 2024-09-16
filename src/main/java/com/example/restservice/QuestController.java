package com.example.restservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes.Name;
import java.lang.Double;
import java.lang.Number;
import java.lang.Integer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Connection; // Add this import statement
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.restservice.VectorController;


@RestController
public class QuestController {

  @Autowired
  JdbcTemplate jdbc;
  ObjectMapper objectMapper = new ObjectMapper();

  // Remove backticks for PostgreSQL and replace ST_AsText with ST_AsEWKT for Well-known text
  String baseQuery = "SELECT id, title, description, city, ST_X(coordinates) AS latitude, ST_Y(coordinates) AS longitude, creator_id, time, time_needed, difficulty FROM \"Quests\"";

  @GetMapping("/getallquests")
  public List<Map<String, Object>> getAllQuests() {
    String sqlQuery = baseQuery;

    List<Map<String, Object>> results = QuestHelper.extractData(sqlQuery, jdbc);

    return results;
  }
  
  @PostMapping("/getquests")
  public Object getQuests(@RequestBody QuestRequest request) {
    try {
      String queryEmbeddingString = VectorController.embedSearchQuery(request.getSearchQuery());
      double latitude = request.getLatitude();
      double longitude = request.getLongitude();
      double radius = request.getRadius();
      List<Map<String, Object>> searchFilters = request.getSearchFilters();
      ArrayList<Object> params = new ArrayList<>();
      StringBuilder sqlQuery = new StringBuilder();
      sqlQuery.append("WITH similarity_calculation AS (\n")
              .append("  SELECT id, title, description, time_needed, difficulty, popularity, coordinates, time, city,\n")
              .append("    (title_embedding <=> ?::vector(768)) AS similarity\n") // similarity based on embedding
              .append("  FROM \"Quests\"\n")
              .append("  WHERE ST_DWithin(coordinates, ST_MakePoint(?, ?)::geography, ?)\n") // location-based filtering
              .append("), \n")
              .append("scores AS (\n")
              .append("  SELECT id, title, description, time_needed, difficulty, popularity, coordinates, time, city, similarity,\n");
      
      // Add the similarity filter param
      params.add(queryEmbeddingString); // for vector embedding
      params.add(latitude); // for ST_MakePoint longitude
      params.add(longitude);  // for ST_MakePoint latitude
      params.add(radius);    // for ST_DWithin radius
      
      // Dynamically generate scoring cases based on filters
      for (Map<String, Object> filter : searchFilters) {
          String filterName = (String) filter.get("filter");
          Boolean ascending = (Boolean) filter.get("ascending");
          Integer weight = (Integer) filter.get("weight");
          
          // Special handling for similarity score
          if (filterName.equals("similarity")) {
              sqlQuery.append("    CASE\n")
                      .append("      WHEN ? THEN (1 - similarity) * ?\n")
                      .append("      ELSE 0\n")
                      .append("    END AS similarity_score,\n");
              params.add(true);  // Flag to include similarity score
              params.add(weight); // Similarity weight
          } else {
              sqlQuery.append("    -- ").append(filterName).append(" Score\n")
                      .append("    CASE \n")
                      .append("      WHEN ").append(filterName).append(" IS NOT NULL AND ? THEN ").append(filterName).append(" * ?\n")
                      .append("      WHEN ").append(filterName).append(" IS NOT NULL AND NOT ?  THEN ").append(filterName).append(" * ?\n")
                      .append("      ELSE 0 \n")
                      .append("    END AS ").append(filterName).append("_score,\n");
              
              // Add parameters for dynamic query
              params.add(ascending); 
              params.add(weight);    
              params.add(ascending); 
              params.add(-1 * weight);    
          }
      }

      // Remove the trailing comma after the last CASE statement
      if (sqlQuery.charAt(sqlQuery.length() - 2) == ',') {
          sqlQuery.setLength(sqlQuery.length() - 2);
      }

      sqlQuery.append("\n  FROM similarity_calculation\n), \n")
              .append("final_scores AS (\n")
              .append("  SELECT *,\n")
              .append("    (");

      // Add weighted score calculation
      boolean first = true;
      for (Map<String, Object> filter : searchFilters) {
          String filterName = (String) filter.get("filter");
          if (first) {
              sqlQuery.append(filterName).append("_score");
              first = false;
          } else {
              sqlQuery.append(" + ").append(filterName).append("_score");
          }
      }

      sqlQuery.append(") AS weighted_score\n")
              .append("  FROM scores\n)\n")
              .append("SELECT id, title, description, time_needed, difficulty, popularity, ST_X(coordinates) AS latitude, ST_Y(coordinates) AS longitude,  time, city, weighted_score\n")
              .append("FROM final_scores\n")
              .append("ORDER BY weighted_score DESC");

      // Execute the dynamically generated query
      System.out.println(sqlQuery.toString());
      for (Object param : params) {
          System.out.println(param);
      }
      List<Map<String, Object>> results = jdbc.queryForList(sqlQuery.toString(), params.toArray());
      System.out.println(results);
      return results;
  }
    catch (Exception e) {
    e.printStackTrace();
    return "Failed to query database: " + e.getMessage();
  }
}

  @PostMapping("/createquest")
  public Object createQuest(@RequestBody QuestRequest request) {

    int id = -1;

    try {
      double latitude = request.getLatitude();
      double longitude = request.getLongitude();
      String city = request.getCity();
      Integer creator_id = request.getCreatorId();
      String description = request.getDescription();
      String title = request.getTitle();
      Integer difficulty = request.getDifficulty();
      Double timeNeeded = request.getTimeNeeded();

      // calculate the city based on the latitude and longitude
      

      RestTemplate restTemplate = new RestTemplate();
      String pythonServiceUrl = "http://localhost:5000/embed";

      // Create the headers
      HttpHeaders headers = new HttpHeaders();
      headers.set("Content-Type", "application/json");

      // Prepare the search text to be sent to the Python API
      Map<String, Object> requestBody = new HashMap<>();
      Map<Integer, String> idTitleMap = new HashMap<>();
      idTitleMap.put(0, description);
      requestBody.put("idTitleMap", idTitleMap);  // Only one entry in the list

      // Convert requestBody to JSON string
      String requestBodyJson = new JSONObject(requestBody).toString();
      System.out.println(requestBodyJson);
      // Send the request to the Python service
      HttpEntity<String> embedRequest = new HttpEntity<>(requestBodyJson, headers);
      System.out.println(embedRequest);
      ResponseEntity<String> response;

      response = restTemplate.exchange(pythonServiceUrl, HttpMethod.POST, embedRequest, String.class);

      JSONObject responseJson = new JSONObject(response.getBody());
          JSONObject idEmbeddingsJson = responseJson.getJSONObject("idEmbeddings");

          if (idEmbeddingsJson.length() != 1) {
              return "Unexpected number of embeddings received from Python service.";
          }

          // Extract the single search embedding
          String searchId = idEmbeddingsJson.keys().next();
          JSONArray searchEmbedding = idEmbeddingsJson.getJSONArray(searchId);
          String searchEmbeddingString = searchEmbedding.toString();

            // PostgreSQL INSERT with ST_GeomFromText for spatial data
            String sqlQuery = "INSERT INTO \"Quests\" " +
            "(title, title_embedding, description, city, coordinates, creator_id, time, difficulty, time_needed) " +
            "VALUES (?, ?::vector(768), ?, ?, ST_GeomFromText(?, 4326), ?, NOW(), ?, ?)";

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbc.update(
            connection -> {
                PreparedStatement ps = connection.prepareStatement(sqlQuery, new String[] { "id" });
                ps.setString(1, title);
                ps.setString(2, searchEmbeddingString);
                ps.setString(3, description);
                ps.setString(4, city);
                // Format the coordinates into WKT (Well-Known Text) for POINT.
                ps.setString(5, String.format("POINT(%f %f)", latitude, longitude));
                ps.setInt(6, creator_id);
                ps.setInt(7, difficulty);
                ps.setDouble(8, timeNeeded);
                return ps;
            },
            keyHolder);


          // Retrieve the generated ID
          Number generatedId = keyHolder.getKey();
          id = generatedId.intValue();
          return QuestHelper.extractData(baseQuery + " WHERE id = " + id, jdbc).get(0);
      
      } catch (Exception e) {
          e.printStackTrace();
          return "Failed to connect to Python service.";
      }
  }
}