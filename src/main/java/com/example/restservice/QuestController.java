package com.example.restservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
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

import java.text.DecimalFormat;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class QuestController {

  @Autowired
  JdbcTemplate jdbc;
  ObjectMapper objectMapper = new ObjectMapper();

  String baseQuery = "SELECT id, title, description, city, ST_AsText(coordinates) AS coordinates, tags, creator_id, time FROM `schema`.`Quests`";

  @GetMapping("/getallquests")
  public List<Map<String, Object>> getAllQuests() {
    String sqlQuery = baseQuery;

    List<Map<String, Object>> results = QuestHelper.extractData(sqlQuery, jdbc);

    return results;
  }

  @PostMapping("/getquestsbydistance")
  public List<Map<String, Object>> getQuestsByDistance(@RequestBody Request request) {

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
    String sqlQuery = String.format(
        baseQuery + " WHERE MBRContains(" +
            "    ST_GeomFromText('POLYGON((" +
            "        %s %s," + // Bottom-left corner
            "        %s %s," + // Bottom-right corner
            "        %s %s," + // Top-right corner
            "        %s %s," + // Top-left corner
            "        %s %s))', 4326), " + // Closing the polygon
            "    coordinates) " +
            "AND ST_Distance_Sphere(" +
            "    coordinates, " +
            "    ST_GeomFromText('POINT(%s %s)', 4326)) " +
            "<= %f",
        df.format(minLat), df.format(minLon), // Bottom-left
        df.format(minLat), df.format(maxLon), // Bottom-right
        df.format(maxLat), df.format(maxLon), // Top-right
        df.format(maxLat), df.format(minLon), // Top-left
        df.format(minLat), df.format(minLon), // Closing
        df.format(latitude), df.format(longitude), // Center point
        radiusKm * 1000 // Convert radius to meters
    );

    List<Map<String, Object>> results = QuestHelper.extractData(sqlQuery, jdbc);

    return results;
  }

  @PostMapping("/getquestbyid")
  public List<Map<String, Object>> getQuestById(@RequestBody Request request) {

    double id = request.getId();

    // Generate SQL query
    String sqlQuery = baseQuery + " WHERE `id` = " + id;

    List<Map<String, Object>> results = QuestHelper.extractData(sqlQuery, jdbc);

    return results;
  }

  @PostMapping("/getquestsbycreator")
  public List<Map<String, Object>> getQuestsByCreator(@RequestBody Request request) {

    double creator_id = request.getCreatorId();

    // Generate SQL query
    String sqlQuery = baseQuery + " WHERE `creator_id` = " + creator_id;

    List<Map<String, Object>> results = QuestHelper.extractData(sqlQuery, jdbc);

    return results;
  }

  @PostMapping("/createquest")
  public int createQuest(@RequestBody Request request) {

    double latitude = request.getCoordinates()[1];
    double longitude = request.getCoordinates()[0];
    double creator_id = request.getCreatorId();
    String city = request.getCity();
    String description = request.getDescription();
    String title = request.getTitle();
    String[] tags = request.getTags();

    int id = -1;

    try {
      // Convert tags array to JSON string directly in SQL preparation
      String tagsJson = objectMapper.writeValueAsString(tags);

      // Generate SQL query
      String sqlQuery = String.format(
          "INSERT INTO `schema`.`Quests` " +
              "(`title`, `description`, `city`, `coordinates`, `tags`, `creator_id`, `time`) " +
              "VALUES (?, ?, ?, ST_GeomFromText('POINT(%f %f)', 4326), ?, ?, NOW())",
          latitude, longitude);

      // Use KeyHolder to retrieve the generated key
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
    }
    return id;
  }
}
