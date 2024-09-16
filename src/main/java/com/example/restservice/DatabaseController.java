package com.example.restservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DatabaseController {

  @Autowired
  JdbcTemplate jdbc;

  @PostMapping("/resetquests")
  public String resetQuestDatabase() {
    String sqlQuery = "DROP TABLE IF EXISTS \"Quests\";";
    jdbc.execute(sqlQuery);

    String sqlQuery2 = "CREATE EXTENSION IF NOT EXISTS postgis;\n" +
        "CREATE EXTENSION IF NOT EXISTS vector;\n" +
        "CREATE TABLE \"Quests\" (\n" +
        "  \"id\" SERIAL PRIMARY KEY,\n" + // SERIAL will auto-increment the primary key
        "  \"title\" VARCHAR(50) NOT NULL,\n" +
        "  \"titleEmbedded\" BOOLEAN DEFAULT FALSE,\n" +
        "  \"title_embedding\" vector(768),\n" + // Assuming a vector extension for embeddings
        "  \"description\" TEXT NOT NULL,\n" + // TEXT type for description
        "  \"city\" VARCHAR(45) NOT NULL,\n" +
        "  \"coordinates\" GEOMETRY(Point, 4326) NOT NULL,\n" + // PostGIS type for geographical coordinates
        "  \"creator_id\" INTEGER NOT NULL,\n" +
        "  \"time\" TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" + // TIMESTAMP without timezone
        "  \"popularity\" NUMERIC(3,1),\n" + // NUMERIC type for popularity with 1 decimal place
        "  \"time_needed\" NUMERIC(4,1),\n" + // NUMERIC type for time_needed with 1 decimal place
        "  \"cost\" INTEGER,\n" + // INTEGER type for cost
        "  \"difficulty\" INTEGER\n" + // INTEGER type for difficulty
        ");\n" +
        "CREATE INDEX ON \"Quests\" USING GIST (\"coordinates\");\n" + // PostGIS spatial index for coordinates
        "CREATE INDEX ON \"Quests\" (\"creator_id\");\n" + // Index for creator_id
        "CREATE INDEX ON \"Quests\" (\"city\");"; // Index for city

    // Execute the SQL query
    jdbc.execute(sqlQuery2);

    return "good";

  }

  @PostMapping("/initquests")
  public String initializeQuestDatabase() {
    String sqlQuery = "INSERT INTO \"Quests\" (title, description, city, coordinates, creator_id, time, time_needed, popularity, difficulty, cost)\n"
        + "VALUES\n"
        + "  ('LA Adventure', 'Explore the streets of LA! Experience the vibrant culture, street art, and eclectic neighborhoods of Los Angeles. Visit famous landmarks, discover hidden gems, and enjoy the lively atmosphere of this bustling city. Whether you are a foodie, a history buff, or an art lover, LA has something to offer everyone.', 'Los Angeles', ST_GeomFromText('POINT(34.0522 -118.2437)', 4326), 75, NOW(), 8, 8.5, 3, 3),\n"
        + "  ('SD Beach Quest', 'Discover the hidden beaches of San Diego. Relax on pristine sands, explore secluded coves, and take in the stunning ocean views. Enjoy beachside activities, from surfing to sunbathing, and savor the local seafood. San Diegos coastline offers a perfect mix of relaxation and adventure.', 'San Diego', ST_GeomFromText('POINT(32.7157 -117.1611)', 4326), 82, NOW(), 6, 7.8, 2, 2),\n"
        + "  ('Belmont Hills Trek', 'Hike through the scenic hills of Belmont. Immerse yourself in nature as you traverse rolling hills and enjoy panoramic views of the surrounding landscapes. The trek offers a variety of trails suited for all levels, providing opportunities to spot local wildlife and experience the tranquility of the great outdoors.', 'Belmont', ST_GeomFromText('POINT(37.5202 -122.2758)', 4326), 63, NOW(), 4, 6.2, 3, 1),\n"
        + "  ('Oakland Urban Exploration', 'A quest to uncover the history and culture of Oakland. Wander through historic neighborhoods, visit local museums, and enjoy the diverse culinary scene. Learn about the citys rich heritage and dynamic arts community, and engage with its vibrant street culture and historic landmarks.', 'Oakland', ST_GeomFromText('POINT(37.8044 -122.2711)', 4326), 91, NOW(), 5, 7.1, 2, 2),\n"
        + "  ('SF Golden Gate Challenge', 'Cross the iconic Golden Gate Bridge and explore San Francisco. Take a leisurely walk or bike ride across one of the worlds most famous landmarks. Afterward, delve into the citys diverse neighborhoods, from Fishermans Wharf to Chinatown, and enjoy its renowned landmarks and attractions.', 'San Francisco', ST_GeomFromText('POINT(37.7749 -122.4194)', 4326), 88, NOW(), 7, 9.2, 2, 3),\n"
        + "  ('Berkeley Campus Tour', 'Tour the famous UC Berkeley campus and surroundings. Explore the historic university grounds, visit notable landmarks, and enjoy the vibrant campus culture. Engage with the local community, discover the universitys role in shaping innovation and education, and explore the charming area around campus.', 'Berkeley', ST_GeomFromText('POINT(37.8716 -122.2727)', 4326), 79, NOW(), 3, 6.8, 1, 1),\n"
        + "  ('Napa Valley Wine Tasting', 'Indulge in a day of wine tasting in the picturesque Napa Valley. Visit renowned wineries, sample a variety of exquisite wines, and learn about the winemaking process. Enjoy gourmet meals at local bistros and take in the beautiful vineyard views. This quest is perfect for wine enthusiasts and food lovers alike.', 'Napa', ST_GeomFromText('POINT(38.2975 -122.2869)', 4326), 95, NOW(), 6, 8.7, 2, 4),\n"
        + "  ('Santa Cruz Boardwalk Fun', 'Spend a day at the Santa Cruz Boardwalk, enjoying classic amusement park rides and games. Explore the charming boardwalk, taste delicious treats, and take a stroll along the beach. The boardwalk offers a mix of nostalgia and excitement with its vintage attractions and lively atmosphere.', 'Santa Cruz', ST_GeomFromText('POINT(36.9741 -122.0308)', 4326), 72, NOW(), 5, 7.9, 1, 2),\n"
        + "  ('Silicon Valley Tech Tour', 'Explore the heart of Silicon Valley and visit the campuses of some of the worlds leading tech companies. Learn about the innovations and history of the technology industry. Engage with cutting-edge technology and discover how the area became a global tech hub.', 'Mountain View', ST_GeomFromText('POINT(37.3861 -122.0838)', 4326), 86, NOW(), 6, 7.5, 2, 3),\n"
        + "  ('Monterey Bay Aquarium Visit', 'Explore the wonders of the ocean at the Monterey Bay Aquarium. Discover a variety of marine life, from jellyfish to sea otters, and learn about ocean conservation efforts. Enjoy interactive exhibits and educational programs that highlight the beauty and complexity of the ocean ecosystem.', 'Monterey', ST_GeomFromText('POINT(36.6002 -121.8947)', 4326), 81, NOW(), 4, 8.9, 1, 3),\n"
        + "  ('Big Sur Coastal Drive', 'Embark on a breathtaking drive along the Big Sur coast. Experience stunning ocean views, rugged cliffs, and picturesque beaches. Make stops at iconic landmarks such as McWay Falls and Bixby Creek Bridge, and enjoy the natural beauty of one of Californias most scenic routes.', 'Big Sur', ST_GeomFromText('POINT(36.2619 -121.8083)', 4326), 69, NOW(), 8, 9.1, 2, 2),\n"
        + "  ('Redwood Forest Hike', 'Discover the majestic Redwood Forest on a scenic hike. Walk among the towering redwoods and enjoy the peaceful ambiance of this ancient forest. Learn about the ecology of these incredible trees and take in the natural beauty of the forests diverse flora and fauna.', 'Arcata', ST_GeomFromText('POINT(40.8652 -124.0833)', 4326), 77, NOW(), 5, 8.3, 3, 1),\n"
        + "  ('Sacramento History Tour', 'Explore the rich history of Californias capital city. Visit historic sites such as the State Capitol, Sutters Fort, and Old Sacramento. Learn about the citys role in the Gold Rush era and its development into a vibrant urban center with diverse cultural attractions.', 'Sacramento', ST_GeomFromText('POINT(38.5816 -121.4944)', 4326), 84, NOW(), 4, 6.7, 1, 2),\n"
        + "  ('Palm Springs Desert Adventure', 'Experience the unique landscape of Palm Springs with a desert adventure. Explore the arid beauty of the region, from sand dunes to palm oases. Enjoy outdoor activities such as hiking, off-roading, and stargazing under the clear desert sky.', 'Palm Springs', ST_GeomFromText('POINT(33.8303 -116.5453)', 4326), 93, NOW(), 6, 7.6, 3, 3),\n"
        + "  ('Lake Tahoe Winter Sports', 'Hit the slopes and enjoy winter sports at Lake Tahoe. Ski, snowboard, or simply enjoy the snow-covered landscape of this popular resort destination. After a day on the mountain, relax at a cozy lodge or enjoy après-ski activities with stunning lake views.', 'Lake Tahoe', ST_GeomFromText('POINT(39.0968 -120.0324)', 4326), 98, NOW(), 8, 9.4, 4, 5),\n"
        + "  ('Hollywood Walk of Fame', 'Take a stroll down the Hollywood Walk of Fame and see the stars of your favorite celebrities embedded in the sidewalk. Explore the nearby attractions, including the TCL Chinese Theatre and the Hollywood Sign. Immerse yourself in the glitz and glamour of Hollywoods entertainment district.', 'Los Angeles', ST_GeomFromText('POINT(34.0928 -118.3287)', 4326), 89, NOW(), 3, 8.8, 1, 2),\n"
        + "  ('Yosemite National Park Exploration', 'Explore the awe-inspiring beauty of Yosemite National Park. Hike to iconic landmarks like El Capitan and Half Dome, and marvel at the stunning waterfalls and giant sequoias. Experience the natural grandeur of the park and enjoy outdoor activities such as rock climbing and wildlife spotting.', 'Yosemite', ST_GeomFromText('POINT(37.8651 -119.5383)', 4326), 96, NOW(), 12, 9.7, 4, 3),\n"
        + "  ('Santa Barbara Wine Country', 'Discover the wine country of Santa Barbara with a tour of its renowned vineyards and wineries. Enjoy wine tastings, gourmet food pairings, and the picturesque landscape of rolling hills and vineyards. Learn about the winemaking process and savor the regions distinct flavors.', 'Santa Barbara', ST_GeomFromText('POINT(34.4208 -119.6982)', 4326), 87, NOW(), 6, 8.2, 2, 4),\n"
        + "  ('San Jose Tech Museum', 'Visit the Tech Museum of Innovation in San Jose and explore interactive exhibits on science and technology. Engage with hands-on displays, learn about cutting-edge innovations, and discover the future of technology. The museum offers a fun and educational experience for all ages.', 'San Jose', ST_GeomFromText('POINT(37.3382 -121.8863)', 4326), 73, NOW(), 4, 7.3, 1, 2),\n"
        + "  ('Monterey Whale Watching', 'Embark on a whale watching tour in Monterey Bay. Witness the majesty of marine giants as they breach and spout in their natural habitat. Enjoy the scenic beauty of the bay and learn about the diverse marine life that inhabits these waters.', 'Monterey', ST_GeomFromText('POINT(36.6002 -121.8947)', 4326), 92, NOW(), 4, 8.6, 2, 4),\n"
        + "  ('Los Angeles Food Tour', 'Savor the diverse culinary scene of Los Angeles on a food tour. Sample dishes from various cuisines, from street food to fine dining, and experience the citys vibrant food culture. Discover hidden gems and popular eateries while exploring different neighborhoods.', 'Los Angeles', ST_GeomFromText('POINT(34.0522 -118.2437)', 4326), 85, NOW(), 5, 8.4, 1, 3),\n"
        + "  ('San Diego Zoo Safari Park', 'Explore the San Diego Zoo Safari Park and experience wildlife in a naturalistic setting. Observe animals from around the world, participate in interactive exhibits, and learn about conservation efforts. The park offers a unique and immersive experience for animal lovers.', 'San Diego', ST_GeomFromText('POINT(33.0953 -116.0500)', 4326), 94, NOW(), 6, 9.0, 2, 4),\n"
        + "  ('Santa Cruz Surfing Adventure', 'Catch some waves in Santa Cruz and experience the thrill of surfing. Whether you are a beginner or an experienced surfer, the beaches offer a variety of surf conditions. Take a lesson, rent equipment, and enjoy the surf culture of this iconic coastal city.', 'Santa Cruz', ST_GeomFromText('POINT(36.9741 -122.0308)', 4326), 78, NOW(), 4, 7.8, 3, 3),\n"
        + "  ('Sacramento River Cruise', 'Enjoy a relaxing river cruise along the Sacramento River. Take in the scenic views of the citys waterfront, learn about the areas history, and enjoy a meal or drinks on board. The cruise offers a peaceful escape and a unique perspective of Sacramentos landmarks.', 'Sacramento', ST_GeomFromText('POINT(38.5816 -121.4944)', 4326), 71, NOW(), 3, 6.9, 1, 3),\n"
        + "  ('Lake Tahoe Scenic Hike', 'Explore the scenic trails around Lake Tahoe. Enjoy stunning views of the lake, surrounding mountains, and alpine forests. The hike offers opportunities for wildlife spotting and relaxing in nature. Choose from a variety of trails to match your skill level and interests.', 'Lake Tahoe', ST_GeomFromText('POINT(39.0968 -120.0324)', 4326), 83, NOW(), 5, 8.7, 3, 2),\n"
        + "  ('Big Bear Lake Outdoor Fun', 'Experience outdoor activities at Big Bear Lake, including hiking, boating, and fishing. The lake offers a range of recreational options, with beautiful mountain scenery and opportunities for adventure. Enjoy a day in the great outdoors with family and friends.', 'Big Bear Lake', ST_GeomFromText('POINT(34.2438 -116.9182)', 4326), 76, NOW(), 7, 7.9, 2, 3),\n"
        + "  ('Palm Springs Art Tour', 'Explore the vibrant art scene of Palm Springs on a guided tour. Visit local galleries, art installations, and public art displays. Learn about the regions artistic heritage and discover the work of both established and emerging artists.', 'Palm Springs', ST_GeomFromText('POINT(33.8303 -116.5453)', 4326), 68, NOW(), 3, 6.8, 1, 2),\n"
        + "  ('Yosemite Photography Expedition', 'Capture the breathtaking beauty of Yosemite National Park on a photography expedition. Explore stunning vistas, iconic landmarks, and serene landscapes. Learn photography techniques and tips to capture the parks natural splendor.', 'Yosemite', ST_GeomFromText('POINT(37.8651 -119.5383)', 4326), 97, NOW(), 8, 8.9, 3, 4),\n"
        + "  ('Santa Barbara Beach Day', 'Relax and unwind on the beautiful beaches of Santa Barbara. Enjoy sunbathing, swimming, and beachside activities. Explore the nearby shops and restaurants, and take in the stunning coastal views. Santa Barbaras beaches offer a perfect day of relaxation and fun.', 'Santa Barbara', ST_GeomFromText('POINT(34.4208 -119.6982)', 4326), 74, NOW(), 6, 8.1, 1, 2),\n"
        + "  ('San Jose Japantown Experience', 'Immerse yourself in the rich culture of San Joses Japantown. Visit local shops, dine at authentic Japanese restaurants, and explore cultural landmarks. Learn about the history and traditions of this vibrant community.', 'San Jose', ST_GeomFromText('POINT(37.3394 -121.8940)', 4326), 80, NOW(), 4, 7.2, 1, 2),\n"
        + "  ('Monterey Scenic Drive', 'Take a leisurely drive along the scenic routes of Monterey. Enjoy stunning coastal views, charming seaside towns, and picturesque landscapes. Stop at scenic overlooks and enjoy the serene beauty of the region.', 'Monterey', ST_GeomFromText('POINT(36.6002 -121.8947)', 4326), 67, NOW(), 5, 8.3, 1, 2);";
        
    jdbc.execute(sqlQuery);
    return "good";
  }

  @PostMapping("/resetusers")
  public String resetUserDatabase() {
    // Drop the "Users" table if it exists
    String sqlQuery = "DROP TABLE IF EXISTS \"Users\";";
    jdbc.execute(sqlQuery);

    // Create the "Users" table
    String sqlQuery2 = "CREATE TABLE \"Users\" (\n" +
        "  id SERIAL PRIMARY KEY,\n" + // SERIAL will auto-increment the primary key
        "  username VARCHAR(255) NOT NULL UNIQUE,\n" + // UNIQUE constraint for username
        "  email VARCHAR(255) NOT NULL UNIQUE,\n" + // UNIQUE constraint for email
        "  password_hash VARCHAR(255) NOT NULL,\n" +
        "  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),\n" +
        "  points INTEGER DEFAULT 0,\n" +
        "  quests JSONB\n" + // JSONB for quests field
        ");";

    // Execute the SQL query
    jdbc.execute(sqlQuery2);

    return "good";
  }

  @PostMapping("/initusers")
  public ResponseEntity<?> initUserDatabase() {
    try {
      // Create a list of default users
      List<Map<String, Object>> defaultUsers = List.of(
          UserHelper.createUser("user1", "user1@example.com", "password123", 0, jdbc),
          UserHelper.createUser("user2", "user2@example.com", "password123", 0, jdbc),
          UserHelper.createUser("user3", "user3@example.com", "password123", 0, jdbc),
          UserHelper.createUser("user4", "user4@example.com", "password123", 0, jdbc),
          UserHelper.createUser("user5", "user5@example.com", "password123", 0, jdbc));

      // Return a success response with the created users
      return ResponseEntity.status(200).body(Map.of(
          "message", "Default users initialized successfully",
          "users", defaultUsers));
    } catch (Exception e) {
      // Return an error response if something goes wrong
      return ResponseEntity.status(400).body(Map.of(
          "message", "Failed to initialize users",
          "error", e.getMessage()));
    }
  }

}
