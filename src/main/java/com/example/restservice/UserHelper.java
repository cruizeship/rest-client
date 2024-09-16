package com.example.restservice;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.stream.IntStream;
import java.util.Arrays;

import java.sql.PreparedStatement;

public class UserHelper {

  static String baseQuery = "SELECT * FROM \"Users\"";

  public static int[] formatIntArray(String input) {
    if (input == null || input.equals("{}") || input.trim().isEmpty()) {
      return new int[0]; // Return an empty array if the input is empty or invalid
    }

    // Remove the square brackets and trim any extra spaces
    String cleanedInput = input.replaceAll("[\\[\\]\\s]", "");

    // Parse the cleaned string into an int array
    return Arrays.stream(cleanedInput.split(","))
        .filter(str -> !str.trim().isEmpty()) // Filter out empty strings
        .mapToInt(Integer::parseInt)
        .toArray();
  }

  public static List<Map<String, Object>> extractData(String sqlQuery, JdbcTemplate jdbc) {

    return jdbc.query(sqlQuery,
        new RowMapper<Map<String, Object>>() {
          @Override
          public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> result = new HashMap<>();

            System.out.println("Extracting data from ResultSet");
            // Null checking for all columns in Quests table
            Integer id = rs.getObject("id") != null ? rs.getInt("id") : null;
            System.out.println("ID: " + id);
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

  public static Map<String, Object> createUser(String username, String email, String password, int points, JdbcTemplate jdbc) {

      // Hash the password before saving it to the database
      String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
      // Modify the SQL query to use the RETURNING clause
      String sqlQuery = "INSERT INTO \"Users\" (username, email, password_hash, points, quests, created_at) " +
      "VALUES (?, ?, ?, ?, '{}'::jsonb, NOW()) RETURNING id";

      // Use KeyHolder to retrieve the generated ID
      KeyHolder keyHolder = new GeneratedKeyHolder();

      jdbc.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(sqlQuery, new String[] { "id" });
      ps.setString(1, username);
      ps.setString(2, email);
      ps.setString(3, passwordHash);
      ps.setInt(4, points);

      return ps;
      }, keyHolder);

      // Retrieve the generated key (ID)
      Number generatedId = keyHolder.getKey();
      System.out.println("Generated user ID: " + generatedId);
        
      int id = generatedId.intValue();

      return extractData(baseQuery + " WHERE id = " + id, jdbc).get(0);
  }
}