package com.example.restservice;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.text.DecimalFormat;

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

  public static String getQuestsByDistance(Request request) {
    double latitude = request.getCoordinates()[1];
    double longitude = request.getCoordinates()[0];
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
            double[] coordinates = rs.getString("coordinates") != null ? formatPoint(rs.getString("coordinates"))
                : new double[0];
            String[] tags = rs.getString("tags") != null ? formatStringArray(rs.getString("tags")) : new String[0]; // Assuming JSON is returned as a
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