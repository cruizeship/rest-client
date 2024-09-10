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

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.restservice.envConfig;

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

  @PostMapping("/getquests")
  public Object getQuests(@RequestBody Request request) {
    String sqlQuery = baseQuery;
    ArrayList<String> sqlAdd = new ArrayList<>();
    try {
      if (request.getId() != null) {
        sqlAdd.add("id = '" + request.getId() + "'");
      }
      if (request.getTitle() != null) {
        sqlAdd.add("title = '" + request.getTitle() + "'");
      }
      if (request.getCreator_id() != null) {
        sqlAdd.add("creator_id = '" + request.getCreator_id() + "'");
      }
      if (request.getCoordinates() != null) {
        if (request.getRadius() != null) {
          sqlAdd.add(QuestHelper.getQuestsByDistance(request));
        }
      }
      if (request.getCity() != null) {
        sqlAdd.add("city = '" + request.getCity() + "'");
      }
      if (request.getTags() != null) {
        for (String tag : request.getTags()) {
          sqlAdd.add(String.format("tags @> '[\"%s\"]'::jsonb", tag)); // PostgreSQL uses jsonb type and @> operator
        }
      }

      for (int i = 0; i < sqlAdd.size(); i++) {
        if (i == 0) {
          sqlQuery += " WHERE ";
        } else {
          sqlQuery += " AND ";
        }
        sqlQuery += sqlAdd.get(i);
      }
      sqlQuery += ";";

      List<Map<String, Object>> results = QuestHelper.extractData(sqlQuery, jdbc);
      return results;

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
      double creator_id = request.getCreator_id();
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
