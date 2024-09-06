package com.example.restservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

@RestController
public class QuestController {

  @Autowired
  JdbcTemplate jdbc;

  /*
  @PostMapping("/adduser")
  public void adduser(@RequestParam(value = "id") long id, @RequestParam(value = "username") String username,
      @RequestParam(value = "value") long value) {
    System.out.println("tyring to add");
    a.addUser(new User(id, username, value));
  }
  */

  // @GetMapping("/insertquest")
  // public String insertQuest() {
  //   jdbc.execute(
  //     "INSERT INTO Quests (title, description, city, coordinates, creator_id) VALUES ('Quest Title 2', 'Description here', 'CityName', ST_GeomFromText('POINT(0 0)'), 123);");  
  //   return "data inserted Successfully";
  // }

  @GetMapping("/insertquest")
  public String insertQuest(
            @RequestParam(value = "title") String title,
            @RequestParam(value = "description") String description,
            @RequestParam(value = "city") String city,
            @RequestParam(value = "location") String location,
            @RequestParam(value = "creatorId") String creatorId,
            @RequestParam(value = "time") String time) {
        
        // Processing logic here
        return String.format("Quest Title: %s, Description: %s, City: %s, Location: %s, Creator ID: %s, Time: %s",
                title, description, city, location, creatorId, time);
    }

  @GetMapping("/getall")
  public List<Map<String, Object>> getall() {
    List<Map<String, Object>> results = jdbc.query("SELECT * FROM dbname.Persons;",
        new RowMapper<Map<String, Object>>() {
          @Override
          public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> result = new HashMap<>();

            // Null checking for all columns
            Integer personId = rs.getObject("PersonID") != null ? rs.getInt("PersonID") : null;
            String lastName = rs.getString("LastName") != null ? rs.getString("LastName") : "";
            String firstName = rs.getString("FirstName") != null ? rs.getString("FirstName") : "";
            String address = rs.getString("Address") != null ? rs.getString("Address") : "";
            String city = rs.getString("City") != null ? rs.getString("City") : "";

            // Populate the map with the checked values
            result.put("PersonID", personId);
            result.put("LastName", lastName);
            result.put("FirstName", firstName);
            result.put("Address", address);
            result.put("City", city);

            return result;
          }
        });
    return results;
  }
  @GetMapping("/getallquests")
public List<Map<String, Object>> getAllQuests() {
    List<Map<String, Object>> results = jdbc.query("SELECT * FROM dbname.Quests;",
        new RowMapper<Map<String, Object>>() {
          @Override
          public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> result = new HashMap<>();

            // Null checking for all columns in Quests table
            Integer id = rs.getObject("id") != null ? rs.getInt("id") : null;
            String title = rs.getString("title") != null ? rs.getString("title") : "";
            String description = rs.getString("description") != null ? rs.getString("description") : "";
            String city = rs.getString("city") != null ? rs.getString("city") : "";
            String coordinates = rs.getString("coordinates") != null ? rs.getString("coordinates") : ""; // Assuming point is returned as a string
            String tags = rs.getString("tags") != null ? rs.getString("tags") : ""; // Assuming JSON is returned as a string
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

    return results;
}

}
