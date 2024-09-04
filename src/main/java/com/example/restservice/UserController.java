package com.example.restservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

  UserList a = new UserList();
  @Autowired
  JdbcTemplate jdbc;

  @GetMapping("/getuser")
  public User getuser(@RequestParam(value = "username") String username) {
    return a.getUser(username);
  }

  @GetMapping("/getuserlist")
  public String getuserlist() {
    System.out.println("getting list");
    return "User List: " + a.toString();
  }

  @PostMapping("/adduser")
  public void adduser(@RequestParam(value = "id") long id, @RequestParam(value = "username") String username,
      @RequestParam(value = "value") long value) {
    System.out.println("tyring to add");
    a.addUser(new User(id, username, value));
  }

  @GetMapping("/insert")
  public String insert() {
    jdbc.execute(
        "INSERT INTO Persons (PersonID, LastName, FirstName, Address, City) VALUES(6, 'fakjl;adsfjalska', 'Entry', 'New home', 'UBC');");
    return "data inserted Successfully";
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
}
