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
public class DatabaseController {

  @Autowired
  JdbcTemplate jdbc;

  @PostMapping("/resetdatabase")
  public String resetDatabase() {
    String sqlQuery = "DROP TABLE IF EXISTS `schema`.`Quests`;";
    jdbc.execute(sqlQuery);
    sqlQuery = "CREATE TABLE `schema`.`Quests` (\n" + //
            "  `id` INT NOT NULL AUTO_INCREMENT,\n" + //
            "  `title` VARCHAR(50) NOT NULL,\n" + //
            "  `description` VARCHAR(100) NOT NULL,\n" + //
            "  `city` VARCHAR(45) NOT NULL,\n" + //
            "  `coordinates` GEOMETRY NOT NULL SRID 4326, -- Enforce SRID for spatial consistency\n" + //
            "  `tags` JSON NULL,\n" + //
            "  `creator_id` INT NOT NULL,\n" + //
            "  `time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" + //
            "  PRIMARY KEY (`id`),\n" + //
            "  UNIQUE INDEX `title_UNIQUE` (`title` ASC),\n" + //
            "  SPATIAL INDEX `coordinates_SPATIAL` (`coordinates`), -- Spatial index\n" + //
            "  INDEX `creator_id_idx` (`creator_id`),               -- Index for creator queries\n" + //
            "  INDEX `city_idx` (`city`)                            -- Index for city queries\n" + //
            ");";
    jdbc.execute(sqlQuery);
    sqlQuery = "ALTER TABLE `schema`.`Quests`\n" + //
            "  MODIFY `coordinates` GEOMETRY NOT NULL SRID 4326,\n" + //
            "  ADD `specific_tag` VARCHAR(255) GENERATED ALWAYS AS (JSON_UNQUOTE(JSON_EXTRACT(`tags`, '$[0]'))) VIRTUAL,\n" + //
            "  ADD INDEX `specific_tag_idx` (`specific_tag`);";
    jdbc.execute(sqlQuery);
    return "good";
  }

  @PostMapping("/initializedatabase")
  public String initializeDatabase() {
    String sqlQuery = "INSERT INTO `schema`.`Quests` (`title`, `description`, `city`, `coordinates`, `tags`, `creator_id`, `time`)\n" + //
            "VALUES\n" + //
            "  ('LA Adventure', 'Explore the streets of LA!', 'Los Angeles', ST_GeomFromText('POINT(34.0522 -118.2437)', 4326), '[\"adventure\", \"urban\", \"discovery\"]', 1, NOW()),\n" + //
            "  ('SD Beach Quest', 'Discover the hidden beaches of San Diego.', 'San Diego', ST_GeomFromText('POINT(32.7157 -117.1611)', 4326), '[\"beach\", \"water\", \"relaxation\"]', 2, NOW()),\n" + //
            "  ('Belmont Hills Trek', 'Hike through the scenic hills of Belmont.', 'Belmont', ST_GeomFromText('POINT(37.5202 -122.2758)', 4326), '[\"hiking\", \"nature\", \"outdoors\"]', 3, NOW()),\n" + //
            "  ('Oakland Urban Exploration', 'A quest to uncover the history and culture of Oakland.', 'Oakland', ST_GeomFromText('POINT(37.8044 -122.2711)', 4326), '[\"culture\", \"history\", \"city\"]', 4, NOW()),\n" + //
            "  ('SF Golden Gate Challenge', 'Cross the iconic Golden Gate Bridge and explore San Francisco.', 'San Francisco', ST_GeomFromText('POINT(37.7749 -122.4194)', 4326), '[\"landmarks\", \"bridge\", \"city\"]', 5, NOW()),\n" + //
            "  ('Berkeley Campus Tour', 'Tour the famous UC Berkeley campus and surroundings.', 'Berkeley', ST_GeomFromText('POINT(37.8716 -122.2727)', 4326), '[\"education\", \"campus\", \"tour\"]', 6, NOW());\n" + //
            "";
    jdbc.execute(sqlQuery);
    return "good";
  }

}