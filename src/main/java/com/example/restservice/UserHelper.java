package com.example.restservice;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.jdbc.core.JdbcTemplate;

public class UserHelper {
  public static int[] formatIntArray(String arrayString) {
    if (arrayString != null && arrayString.startsWith("[") && arrayString.endsWith("]")) {
      // Remove the brackets [ ]
      String content = arrayString.substring(1, arrayString.length() - 1);

      // Split by comma, making sure to trim any whitespace
      String[] parts = content.split(",");

      // Create an array to hold the integers
      int[] intArray = new int[parts.length];

      // Parse each string part into an integer and store in intArray
      for (int i = 0; i < parts.length; i++) {
        parts[i] = parts[i].trim(); // Remove surrounding whitespace
        intArray[i] = Integer.parseInt(parts[i]); // Convert to integer
      }

      return intArray;
    }

    // Return an empty array if the input is not valid
    return new int[0];
  }

  public static List<Map<String, Object>> extractData(String sqlQuery, JdbcTemplate jdbc) {
    return jdbc.query(sqlQuery,
        new RowMapper<Map<String, Object>>() {
          @Override
          public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> result = new HashMap<>();

            // Null checking for all columns in Quests table
            Integer id = rs.getObject("id") != null ? rs.getInt("id") : null;
            String email = rs.getString("email") != null ? rs.getString("email") : "";
            String username = rs.getString("username") != null ? rs.getString("username") : "";
            String password = rs.getString("password_hash") != null ? rs.getString("password_hash") : "";
            String time = rs.getString("created_at") != null ? rs.getString("created_at") : "";

            Integer points = rs.getObject("points") != null ? rs.getInt("points") : null;
            int[] quests = rs.getString("quests") != null ? formatIntArray(rs.getString("quests")) : new int[0];

            // Populate the map with the checked values
            result.put("id", id);
            result.put("email", email);
            result.put("username", username);
            result.put("password_hash", password);
            result.put("points", points);
            result.put("quests", quests);
            result.put("created_at", time != null ? time.toString() : null); // Converting Timestamp to String if needed

            return result;
          }
        });
  }
}