package com.example.restservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;
import java.util.ArrayList;

@RestController
public class QuestController {

  @Autowired
  JdbcTemplate jdbc;

  String baseQuery = "SELECT id, title, description, city, ST_AsText(coordinates) AS coordinates, tags, creator_id, time FROM `schema`.`Quests`";

  /*
   * @PostMapping("/adduser")
   * public void adduser(@RequestParam(value = "id") long id, @RequestParam(value
   * = "username") String username,
   * 
   * @RequestParam(value = "value") long value) {
   * System.out.println("tyring to add");
   * a.addUser(new User(id, username, value));
   * }
   */

  // @GetMapping("/insertquest")
  // public String insertQuest() {
  // jdbc.execute(
  // "INSERT INTO Quests (title, description, city, coordinates, creator_id)
  // VALUES ('Quest Title 2', 'Description here', 'CityName',
  // ST_GeomFromText('POINT(0 0)'), 123);");
  // return "data inserted Successfully";
  // }

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

}
