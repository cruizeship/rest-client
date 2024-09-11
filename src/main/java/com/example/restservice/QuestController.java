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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.RestController;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.restservice.envConfig;

import com.example.restservice.VectorController;


@RestController
public class QuestController {

  @Autowired
  JdbcTemplate jdbc;
  ObjectMapper objectMapper = new ObjectMapper();

  String DB_NAME = envConfig.getDB_NAME();
  // Remove backticks for PostgreSQL and replace ST_AsText with ST_AsEWKT for Well-known text
  String baseQuery = String.format("SELECT id, title, description, city, ST_AsEWKT(coordinates) AS coordinates, tags, creator_id, time FROM %s.\"Quests\"", DB_NAME);

  @GetMapping("/getallquests")
  public List<Map<String, Object>> getAllQuests() {
    String sqlQuery = baseQuery;

    List<Map<String, Object>> results = QuestHelper.extractData(sqlQuery, jdbc);

    return results;
  }
  // search quests by different filters 
  // search radius 
  // search filters 
  // semantic search term 
  @PostMapping("/getquests")
  public Object getQuests(@RequestBody Request request) {
    
    String queryEmbeddingString = VectorController.embedSearchQuery(request.getSearchQuery());
    double latitude = request.getCoordinates()[1];
    double longitude = request.getCoordinates()[0];
    double radius = request.getRadius();
    boolean sortTimeNeeded = request.getSortTimeNeeded();
    boolean sortDifficulty = request.getSortDifficulty();
    boolean sortPopularity = request.getSortPopularity();
    boolean useSimilarityWeight = request.getUseSimilarityWeight();
    double timeWeight = request.getTimeWeight();
    double difficultyWeight = request.getDifficultyWeight();
    double popularityWeight = request.getPopularityWeight();
    double similarityWeight = request.getSimilarityWeight();
    //double timeWeight = 1.0;
    //double difficultyWeight = 1.0;
    //double popularityWeight = 1.0;
    //double similarityWeight = 1.0;
    //System.out.println("Coordinates: " + latitude + ", " + longitude);
    try {
      // Define the SQL query with placeholders for parameters
      //Step 1: Query for similarity_cte
      String sqlQuery = 
        "WITH similarity_calculation AS (\n" +
        "  SELECT id, title, description, time_needed, difficulty, popularity, coordinates, time, city, \n" +
        "    (title_embedding <=> ?::vector(768)) AS similarity\n" +
        "  FROM sidequests.\"Quests\"\n" +
        "  WHERE ST_DWithin(coordinates, ST_MakePoint(?, ?)::geography, ?)\n" +
        "), \n" +
        "scores AS (\n" +
        "  SELECT id, title, description, time_needed, difficulty, popularity, coordinates, time, city, similarity,\n" +
        "    -- Time Score\n" +
        "    CASE \n" +
        "      WHEN time_needed IS NOT NULL AND ? THEN time_needed * ?\n" +
        "      WHEN time_needed IS NOT NULL AND NOT ? THEN (50 - time_needed) * ?\n" +
        "      ELSE 0 \n" +
        "    END AS time_score,\n" +
        "    -- Difficulty Score\n" +
        "    CASE \n" +
        "      WHEN difficulty IS NOT NULL AND ? THEN difficulty * ?\n" +
        "      WHEN difficulty IS NOT NULL AND NOT ? THEN (5 - difficulty) * ?\n" +
        "      ELSE 0\n" +
        "    END AS difficulty_score,\n" +
        "    -- Popularity Score\n" +
        "    CASE \n" +
        "      WHEN popularity IS NOT NULL AND ? THEN popularity * ?\n" +
        "      WHEN popularity IS NOT NULL AND NOT ? THEN (10 - popularity) * ?\n" +
        "      ELSE 0\n" +
        "    END AS popularity_score,\n" +
        "    -- Similarity Score\n" +
        "    CASE \n" +
        "      WHEN ? THEN (1 - similarity) * ?\n" +
        "      ELSE 0\n" +
        "    END AS similarity_score\n" +
        "  FROM similarity_calculation\n" +
        "), \n" +
        "final_scores AS (\n" +
        "  SELECT *,\n" +
        "    (time_score + difficulty_score + popularity_score + similarity_score) AS weighted_score\n" +
        "  FROM scores\n" +
        ")\n" +
        "SELECT id, title, description, time_needed, difficulty, popularity, coordinates, time, city, weighted_score, similarity_scor\n" +
        "FROM final_scores\n" +
        "ORDER BY weighted_score DESC";

Object[] params = {
    queryEmbeddingString, // 1: Embedding vector
    longitude,            // 2: Longitude for location search
    latitude,             // 3: Latitude for location search
    radius,               // 4: Radius for location search
    sortTimeNeeded,       // 5: Boolean for time_needed sorting
    timeWeight,           // 6: Weight for time_needed
    sortTimeNeeded,      // 7: Inverse boolean for low time_needed sorting
    timeWeight,           // 8: Weight for time_needed
    sortDifficulty,       // 9: Boolean for difficulty sorting
    difficultyWeight,     // 10: Weight for difficulty
    !sortDifficulty,      // 11: Inverse boolean for low difficulty sorting
    difficultyWeight,     // 12: Weight for difficulty
    sortPopularity,       // 13: Boolean for popularity sorting
    popularityWeight,     // 14: Weight for popularity
    !sortPopularity,      // 15: Inverse boolean for low popularity sorting
    popularityWeight,     // 16: Weight for popularity
    useSimilarityWeight,   // 17: Boolean for similarity weighting
    similarityWeight      // 18: Weight for similarity
};
      
      return jdbc.queryForList(sqlQuery, params);

    
    } catch (Exception e) {
      e.printStackTrace();
      return "Failed to query database: " + e.getMessage();
    }
  }

  @PostMapping("/createquest")
  public Object createQuest(@RequestBody Request request) {

    int id = -1;

    try {
      double latitude = request.getCoordinates()[1];
      double longitude = request.getCoordinates()[0];
      double creator_id = request.getCreatorId();
      String city = request.getCity();
      String description = request.getDescription();
      String title = request.getTitle();
      String[] tags = request.getTags();

      // Convert tags array to JSON string directly in SQL preparation
      String tagsJson = objectMapper.writeValueAsString(tags);

      // PostgreSQL INSERT with ST_GeomFromText for spatial data
      String sqlQuery = String.format(
          "INSERT INTO %s.\"Quests\" " +
              "(title, description, city, coordinates, tags, creator_id, time) " +
              "VALUES (?, ?, ?, ST_GeomFromText('POINT(%f %f)', 4326), ?, ?, NOW())",
          DB_NAME, latitude, longitude);

      KeyHolder keyHolder = new GeneratedKeyHolder();

      jdbc.update(
          connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, new String[] { "id" });
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, city);
            ps.setString(4, tagsJson);
            ps.setDouble(5, creator_id);
            return ps;
          },
          keyHolder);

      // Retrieve the generated ID
      Number generatedId = keyHolder.getKey();
      id = generatedId.intValue();

    } catch (Exception e) {
      e.printStackTrace();
      return "Failed to add quest: " + e.getMessage();
    }

    return QuestHelper.extractData(baseQuery + " WHERE id = " + id, jdbc).get(0);
  }

}
