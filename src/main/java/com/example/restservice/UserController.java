package com.example.restservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.lang.Arrays;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.sql.PreparedStatement;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class UserController {

  @Autowired
  JdbcTemplate jdbc;
  ObjectMapper objectMapper = new ObjectMapper();

  String baseQuery = "SELECT * FROM `schema`.`Users`";

  public static boolean verifyPassword(String plainPassword, String hashedPassword) {
    return BCrypt.checkpw(plainPassword, hashedPassword);
  }

  @PostMapping("/getuser")
  public Object getUser(@RequestBody UserRequest request) {
    String sqlQuery = baseQuery;
    ArrayList<String> sqlAdd = new ArrayList<>();
    try {
      // Check if the fields in the UserRequest object are not null (or valid)
      if (request.getId() != null) {
        sqlAdd.add("`id` = '" + request.getId() + "'");
      }
      if (request.getUsername() != null) {
        sqlAdd.add("`username` = '" + request.getUsername() + "'");
      }
      if (request.getEmail() != null) {
        sqlAdd.add("`email` = '" + request.getEmail() + "'");
      }
      if (request.getPassword() != null) {
        sqlAdd.add("`password_hash` = '" + request.getPassword() + "'");
      }
      if (request.getPoints() != null) {
        sqlAdd.add("`points` = '" + request.getPoints() + "'");
      }
      if (request.getTime() != null) {
        sqlAdd.add("`time` = '" + request.getTime() + "'");
      }
      if (request.getQuests() != null && request.getQuests().length > 0) {
        // For array of quests, we can join them with commas and wrap in parentheses
        for (int quest : request.getQuests()) {
          sqlAdd.add(String.format("JSON_CONTAINS(quests, '\"%d\"', '$')", quest));
        }
      }

      // Add conditions if there are any in the sqlAdd list
      for (int i = 0; i < sqlAdd.size(); i++) {
        if (i == 0) {
          sqlQuery += " WHERE ";
        } else {
          sqlQuery += " AND ";
        }
        sqlQuery += sqlAdd.get(i);
      }
      sqlQuery += ";";
      System.out.println(sqlQuery);

      // Execute the SQL query and get the results
      List<Map<String, Object>> results = UserHelper.extractData(sqlQuery, jdbc);
      return results;

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @PostMapping("/createuser")
  public Object createUser(@RequestBody UserRequest request) {
    try {
      String username = request.getUsername();
      String email = request.getEmail();
      String password = request.getPassword();
      int points = request.getPoints();
      String questsJson = objectMapper.writeValueAsString(request.getQuests());

      // Hash the password before saving it to the database
      String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());

      // Generate SQL query to insert the new user
      String sqlQuery = "INSERT INTO `schema`.`Users` " +
          "(`username`, `email`, `password_hash`, `points`,`quests`, `created_at`) " +
          "VALUES (?, ?, ?, ?, ?, NOW())";

      // Use KeyHolder to retrieve the generated ID
      KeyHolder keyHolder = new GeneratedKeyHolder();

      jdbc.update(
          connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, new String[] { "id" });
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.setInt(4, points);
            ps.setString(5, questsJson);
            return ps;
          },
          keyHolder);

      // Retrieve the generated ID
      Number generatedId = keyHolder.getKey();
      int id = generatedId.intValue();

      return UserHelper.extractData(baseQuery + " WHERE id = " + id, jdbc).get(0);

    } catch (Exception e) {
      e.printStackTrace();
      return "Failed to add user: " + e.getMessage();
    }

  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
    try {
      String email = request.get("email");
      String password = request.get("password");

      // Find the user by email
      List<Map<String, Object>> userList = UserHelper.extractData("SELECT * FROM `schema`.`Users` WHERE `email` = '" + email + "';", jdbc);
      if (userList.size() == 0) {
        return ResponseEntity.status(400).body("No account associated with this email. Please register.");
      }

      Map<String, Object> user = userList.get(0);

      // Verify the password using bcrypt
      if (!verifyPassword(password, user.get("password_hash").toString())) {
        return ResponseEntity.status(400).body("Incorrect password.");
      }
      
      return ResponseEntity.status(200).body("Good.");

    } catch (Exception e) {
      return ResponseEntity.status(500).body("Server error.");
    }
  }

  @GetMapping("/getallusers")
  public List<Map<String, Object>> getAllUsers() {
    String sqlQuery = baseQuery;

    List<Map<String, Object>> results = UserHelper.extractData(sqlQuery, jdbc);

    return results;
  }
}
