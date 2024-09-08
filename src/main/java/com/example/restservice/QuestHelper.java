package com.example.restservice;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.jdbc.core.JdbcTemplate;

public class QuestHelper {
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
            double[] coordinates = rs.getString("coordinates") != null ? formatPoint(rs.getString("coordinates"))
                : new double[0];
            String tags = rs.getString("tags") != null ? rs.getString("tags") : ""; // Assuming JSON is returned as a
                                                                                    // string
            Integer creatorId = rs.getObject("creator_id") != null ? rs.getInt("creator_id") : null;
            Timestamp time = rs.getTimestamp("time") != null ? rs.getTimestamp("time") : null;

            // Populate the map with the checked values
            result.put("id", id);
            result.put("title", title);
            result.put("description", description);
            result.put("city", city);
            result.put("coordinates", coordinates);
            result.put("tags", tags);
            result.put("creator_id", creatorId);
            result.put("time", time != null ? time.toString() : null); // Converting Timestamp to String if needed

            return result;
          }
        });
  }
}