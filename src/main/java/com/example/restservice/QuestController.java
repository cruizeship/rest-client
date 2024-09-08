package com.example.restservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.text.DecimalFormat;

@RestController
public class QuestController {

  @Autowired
  JdbcTemplate jdbc;

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
  public String createQuest(@RequestBody Request request) {

    double latitude = request.getCoordinates()[1];
    double longitude = request.getCoordinates()[0];
    double creator_id = request.getCreatorId();
    String city = request.getCity();
    String description = request.getDescription();
    String title = request.getTitle();
    String tags = request.getTags();

    String sqlQuery = String.format("INSERT INTO `schema`.`Quests` " +
    "(`title`, `description`, `city`, `coordinates`, `tags`, `creator_id`, `time`) " +
    "VALUES ('%s', '%s', '%s', ST_GeomFromText('POINT(%f %f)', 4326), '%s', %f, NOW())",
    title, description, city, latitude, longitude, tags, creator_id);

    jdbc.execute(sqlQuery);

    return sqlQuery;
  }

}
