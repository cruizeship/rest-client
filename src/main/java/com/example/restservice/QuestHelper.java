package com.example.restservice;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.client.RestTemplate;


import java.sql.PreparedStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.text.DecimalFormat;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

public class QuestHelper {

  static String baseQuery = "SELECT id, title, description, city, ST_X(coordinates) AS latitude, ST_Y(coordinates) AS longitude, creator_id, time, time_needed, difficulty FROM \"Quests\"";

  public static double[] formatPoint(String point) {
    if (point != null && point.startsWith("POINT(") && point.endsWith(")")) {
      String content = point.substring(6, point.length() - 1); // Remove 'POINT(' and ')'
      String[] parts = content.split(" ");
      if (parts.length == 2) {
        try {
          double latitude = Double.parseDouble(parts[0]);
          double longitude = Double.parseDouble(parts[1]);
          return new double[] { longitude, latitude };
        } catch (NumberFormatException e) {
          // Handle the case where parsing fails
          e.printStackTrace();
        }
      }
    }
    // Return an empty array if the format is incorrect or parsing fails
    return new double[] {};
  }

  public static String[] formatStringArray(String arrayString) {
    if (arrayString != null && arrayString.startsWith("[") && arrayString.endsWith("]")) {
      // Remove the brackets [ ]
      String content = arrayString.substring(1, arrayString.length() - 1);

      // Split by comma, making sure to trim any whitespace or quotes
      String[] parts = content.split(",");

      // Clean up each part (removing any extra spaces or quotes around the elements)
      for (int i = 0; i < parts.length; i++) {
        parts[i] = parts[i].trim().replaceAll("^\"|\"$", ""); // Remove surrounding quotes if they exist
      }
      return parts;
    }

    // Return an empty array if the format is incorrect
    return new String[] {};
  }

  public static String getQuestsByDistance(QuestRequest request) {
    double latitude = request.getLatitude();
    double longitude = request.getLongitude();
    double radiusMiles = request.getRadius();
    double radiusKm = radiusMiles * 1.60934;

    // Calculate latitude and longitude offsets
    double latOffset = radiusKm / 111.0;
    double lonOffset = radiusKm / (111.0 * Math.cos(Math.toRadians(latitude)));

    // Bounding box coordinates
    double minLat = latitude - latOffset;
    double maxLat = latitude + latOffset;
    double minLon = longitude - lonOffset;
    double maxLon = longitude + lonOffset;

    // Format coordinates to 4 decimal places
    DecimalFormat df = new DecimalFormat("0.####");

    // Generate SQL query
    return String.format("ST_Within(" +
        "ST_GeomFromText('POLYGON((" +
        "%s %s," + // Bottom-left corner
        "%s %s," + // Bottom-right corner
        "%s %s," + // Top-right corner
        "%s %s," + // Top-left corner
        "%s %s))', 4326), " + // Closing the polygon
        "coordinates) " +
        "AND ST_Distance(" +
        "coordinates::geography, " +
        "ST_GeomFromText('POINT(%s %s)', 4326)::geography) " +
        "<= %f",
        df.format(minLat), df.format(minLon), // Bottom-left
        df.format(minLat), df.format(maxLon), // Bottom-right
        df.format(maxLat), df.format(maxLon), // Top-right
        df.format(maxLat), df.format(minLon), // Top-left
        df.format(minLat), df.format(minLon), // Closing
        df.format(latitude), df.format(longitude), // Center point
        radiusKm * 1000 // Convert radius to meters
    );
  }

  public static List<Map<String, Object>> extractData(String sqlQuery, JdbcTemplate jdbc) {
    return jdbc.query(sqlQuery,
        new RowMapper<Map<String, Object>>() {
          @Override
          public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> result = new HashMap<>();

            // Null checking for all columns in Quests table
            Integer id = rs.getObject("id") != null ? rs.getInt("id") : null;
            String title = rs.getString("title") != null ? rs.getString("title") : "";
            String description = rs.getString("description") != null ? rs.getString("description") : "";
            String city = rs.getString("city") != null ? rs.getString("city") : "";

            // Handling latitude and longitude
            Double latitude = rs.getObject("latitude") != null ? rs.getDouble("latitude") : null;
            Double longitude = rs.getObject("longitude") != null ? rs.getDouble("longitude") : null;
            double[] coordinates = (latitude != null && longitude != null) ? new double[] { latitude, longitude }
                : new double[0];

            Integer creatorId = rs.getObject("creator_id") != null ? rs.getInt("creator_id") : null;
            Timestamp time = rs.getTimestamp("time") != null ? rs.getTimestamp("time") : null;

            // Handling time_needed and difficulty
            Integer timeNeeded = rs.getObject("time_needed") != null ? rs.getInt("time_needed") : null;
            String difficulty = rs.getString("difficulty") != null ? rs.getString("difficulty") : "";

            // Populate the map with the checked values
            result.put("id", id);
            result.put("title", title);
            result.put("description", description);
            result.put("city", city);
            result.put("coordinates", coordinates);
            result.put("creator_id", creatorId);
            result.put("time", time != null ? time.toString() : null); // Converting Timestamp to String if needed
            result.put("time_needed", timeNeeded);
            result.put("difficulty", difficulty);

            return result;
          }
        });
  }

  public static void associateQuestWithUser(Integer userId, Integer questId, JdbcTemplate jdbc) {
    // SQL query to update the JSONB field by appending the new quest ID
    String sql = "UPDATE \"Users\" SET quests_created = COALESCE(quests_created, '[]'::jsonb) || to_jsonb(?) WHERE id = ?";
    
    // Execute the update
    jdbc.update(sql, questId, userId);
}

  public static Object createQuest(double latitude, double longitude, String city, Integer creator_id,
      String description, String title, Integer difficulty, Double timeNeeded, JdbcTemplate jdbc) {
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
    requestBody.put("idTitleMap", idTitleMap); // Only one entry in the list

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
    int id = generatedId.intValue();

    associateQuestWithUser(creator_id, id, jdbc);
    return QuestHelper.extractData(baseQuery + " WHERE id = " + id, jdbc).get(0);

  }

}