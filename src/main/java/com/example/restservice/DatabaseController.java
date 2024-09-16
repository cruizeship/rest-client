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
        "  \"title\" VARCHAR(50) NOT NULL UNIQUE,\n" +
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
    String sqlQuery = "INSERT INTO \"Quests\" (\"title\", \"description\", \"city\", \"coordinates\", \"tags\", \"creator_id\", \"time\")\n"
        + //
        "VALUES\n" + //
        "  ('LA Adventure', 'Explore the streets of LA! Experience the vibrant culture, street art, and eclectic neighborhoods of Los Angeles. Visit famous landmarks, discover hidden gems, and enjoy the lively atmosphere of this bustling city. Whether you are a foodie, a history buff, or an art lover, LA has something to offer everyone.', 'Los Angeles', ST_GeomFromText('POINT(34.0522 -118.2437)', 4326), '[\"adventure\", \"urban\", \"discovery\"]', 1, NOW()),\n"
        + //
        "  ('SD Beach Quest', 'Discover the hidden beaches of San Diego. Relax on pristine sands, explore secluded coves, and take in the stunning ocean views. Enjoy beachside activities, from surfing to sunbathing, and savor the local seafood. San Diego’s coastline offers a perfect mix of relaxation and adventure.', 'San Diego', ST_GeomFromText('POINT(32.7157 -117.1611)', 4326), '[\"beach\", \"water\", \"relaxation\"]', 2, NOW()),\n"
        + //
        "  ('Belmont Hills Trek', 'Hike through the scenic hills of Belmont. Immerse yourself in nature as you traverse rolling hills and enjoy panoramic views of the surrounding landscapes. The trek offers a variety of trails suited for all levels, providing opportunities to spot local wildlife and experience the tranquility of the great outdoors.', 'Belmont', ST_GeomFromText('POINT(37.5202 -122.2758)', 4326), '[\"hiking\", \"nature\", \"outdoors\"]', 3, NOW()),\n"
        + //
        "  ('Oakland Urban Exploration', 'A quest to uncover the history and culture of Oakland. Wander through historic neighborhoods, visit local museums, and enjoy the diverse culinary scene. Learn about the city’s rich heritage and dynamic arts community, and engage with its vibrant street culture and historic landmarks.', 'Oakland', ST_GeomFromText('POINT(37.8044 -122.2711)', 4326), '[\"culture\", \"history\", \"city\"]', 4, NOW()),\n"
        + //
        "  ('SF Golden Gate Challenge', 'Cross the iconic Golden Gate Bridge and explore San Francisco. Take a leisurely walk or bike ride across one of the world’s most famous landmarks. Afterward, delve into the city’s diverse neighborhoods, from Fisherman’s Wharf to Chinatown, and enjoy its renowned landmarks and attractions.', 'San Francisco', ST_GeomFromText('POINT(37.7749 -122.4194)', 4326), '[\"landmarks\", \"bridge\", \"city\"]', 5, NOW()),\n"
        + //
        "  ('Berkeley Campus Tour', 'Tour the famous UC Berkeley campus and surroundings. Explore the historic university grounds, visit notable landmarks, and enjoy the vibrant campus culture. Engage with the local community, discover the university’s role in shaping innovation and education, and explore the charming area around campus.', 'Berkeley', ST_GeomFromText('POINT(37.8716 -122.2727)', 4326), '[\"education\", \"campus\", \"tour\"]', 6, NOW()),\n"
        + //
        "  ('Napa Valley Wine Tasting', 'Indulge in a day of wine tasting in the picturesque Napa Valley. Visit renowned wineries, sample a variety of exquisite wines, and learn about the winemaking process. Enjoy gourmet meals at local bistros and take in the beautiful vineyard views. This quest is perfect for wine enthusiasts and food lovers alike.', 'Napa', ST_GeomFromText('POINT(38.2975 -122.2869)', 4326), '[\"wine\", \"food\", \"scenery\"]', 7, NOW()),\n"
        + //
        "  ('Santa Cruz Boardwalk Fun', 'Spend a day at the Santa Cruz Boardwalk, enjoying classic amusement park rides and games. Explore the charming boardwalk, taste delicious treats, and take a stroll along the beach. The boardwalk offers a mix of nostalgia and excitement with its vintage attractions and lively atmosphere.', 'Santa Cruz', ST_GeomFromText('POINT(36.9741 -122.0308)', 4326), '[\"amusement\", \"beach\", \"family\"]', 8, NOW()),\n"
        + //
        "  ('Silicon Valley Tech Tour', 'Explore the heart of Silicon Valley and visit the campuses of some of the world’s leading tech companies. Learn about the innovations and history of the technology industry. Engage with cutting-edge technology and discover how the area became a global tech hub.', 'Mountain View', ST_GeomFromText('POINT(37.3861 -122.0838)', 4326), '[\"technology\", \"innovation\", \"industry\"]', 9, NOW()),\n"
        + //
        "  ('Monterey Bay Aquarium Visit', 'Explore the wonders of the ocean at the Monterey Bay Aquarium. Discover a variety of marine life, from jellyfish to sea otters, and learn about ocean conservation efforts. Enjoy interactive exhibits and educational programs that highlight the beauty and complexity of the ocean ecosystem.', 'Monterey', ST_GeomFromText('POINT(36.6002 -121.8947)', 4326), '[\"aquarium\", \"marine life\", \"education\"]', 10, NOW()),\n"
        + //
        "  ('Big Sur Coastal Drive', 'Embark on a breathtaking drive along the Big Sur coast. Experience stunning ocean views, rugged cliffs, and picturesque beaches. Make stops at iconic landmarks such as McWay Falls and Bixby Creek Bridge, and enjoy the natural beauty of one of California’s most scenic routes.', 'Big Sur', ST_GeomFromText('POINT(36.2619 -121.8083)', 4326), '[\"scenery\", \"road trip\", \"nature\"]', 11, NOW()),\n"
        + //
        "  ('Redwood Forest Hike', 'Discover the majestic Redwood Forest on a scenic hike. Walk among the towering redwoods and enjoy the peaceful ambiance of this ancient forest. Learn about the ecology of these incredible trees and take in the natural beauty of the forest’s diverse flora and fauna.', 'Arcata', ST_GeomFromText('POINT(40.8652 -124.0833)', 4326), '[\"hiking\", \"nature\", \"forest\"]', 12, NOW()),\n"
        + //
        "  ('Sacramento History Tour', 'Explore the rich history of California’s capital city. Visit historic sites such as the State Capitol, Sutter’s Fort, and Old Sacramento. Learn about the city’s role in the Gold Rush era and its development into a vibrant urban center with diverse cultural attractions.', 'Sacramento', ST_GeomFromText('POINT(38.5816 -121.4944)', 4326), '[\"history\", \"culture\", \"landmarks\"]', 13, NOW()),\n"
        + //
        "  ('Palm Springs Desert Adventure', 'Experience the unique landscape of Palm Springs with a desert adventure. Explore the arid beauty of the region, from sand dunes to palm oases. Enjoy outdoor activities such as hiking, off-roading, and stargazing under the clear desert sky.', 'Palm Springs', ST_GeomFromText('POINT(33.8303 -116.5453)', 4326), '[\"desert\", \"adventure\", \"outdoors\"]', 14, NOW()),\n"
        + //
        "  ('Lake Tahoe Winter Sports', 'Hit the slopes and enjoy winter sports at Lake Tahoe. Ski, snowboard, or simply enjoy the snow-covered landscape of this popular resort destination. After a day on the mountain, relax at a cozy lodge or enjoy après-ski activities with stunning lake views.', 'Lake Tahoe', ST_GeomFromText('POINT(39.0968 -120.0324)', 4326), '[\"winter sports\", \"skiing\", \"snowboarding\"]', 15, NOW()),\n"
        + //
        "  ('Hollywood Walk of Fame', 'Take a stroll down the Hollywood Walk of Fame and see the stars of your favorite celebrities embedded in the sidewalk. Explore the nearby attractions, including the TCL Chinese Theatre and the Hollywood Sign. Immerse yourself in the glitz and glamour of Hollywood’s entertainment district.', 'Los Angeles', ST_GeomFromText('POINT(34.0928 -118.3287)', 4326), '[\"Hollywood\", \"tourist attractions\", \"celebrity\"]', 16, NOW()),\n"
        + //
        "  ('Yosemite National Park Exploration', 'Explore the awe-inspiring beauty of Yosemite National Park. Hike to iconic landmarks like El Capitan and Half Dome, and marvel at the stunning waterfalls and giant sequoias. Experience the natural grandeur of the park and enjoy outdoor activities such as rock climbing and wildlife spotting.', 'Yosemite', ST_GeomFromText('POINT(37.8651 -119.5383)', 4326), '[\"national park\", \"hiking\", \"nature\"]', 17, NOW()),\n"
        + //
        "  ('Santa Barbara Wine Country', 'Discover the wine country of Santa Barbara with a tour of its renowned vineyards and wineries. Enjoy wine tastings, gourmet food pairings, and the picturesque landscape of rolling hills and vineyards. Learn about the winemaking process and savor the region’s distinct flavors.', 'Santa Barbara', ST_GeomFromText('POINT(34.4208 -119.6982)', 4326), '[\"wine\", \"tasting\", \"scenery\"]', 18, NOW()),\n"
        + //
        "  ('San Jose Tech Museum', 'Visit the Tech Museum of Innovation in San Jose and explore interactive exhibits on science and technology. Engage with hands-on displays, learn about cutting-edge innovations, and discover the future of technology. The museum offers a fun and educational experience for all ages.', 'San Jose', ST_GeomFromText('POINT(37.3382 -121.8863)', 4326), '[\"technology\", \"museum\", \"education\"]', 19, NOW()),\n"
        + //
        "  ('Monterey Whale Watching', 'Embark on a whale watching tour in Monterey Bay. Witness the majesty of marine giants as they breach and spout in their natural habitat. Enjoy the scenic beauty of the bay and learn about the diverse marine life that inhabits these waters.', 'Monterey', ST_GeomFromText('POINT(36.6002 -121.8947)', 4326), '[\"whale watching\", \"marine life\", \"adventure\"]', 20, NOW()),\n"
        + //
        "  ('Los Angeles Food Tour', 'Savor the diverse culinary scene of Los Angeles on a food tour. Sample dishes from various cuisines, from street food to fine dining, and experience the city’s vibrant food culture. Discover hidden gems and popular eateries while exploring different neighborhoods.', 'Los Angeles', ST_GeomFromText('POINT(34.0522 -118.2437)', 4326), '[\"food\", \"tour\", \"cuisine\"]', 21, NOW()),\n"
        + //
        "  ('San Diego Zoo Safari Park', 'Explore the San Diego Zoo Safari Park and experience wildlife in a naturalistic setting. Observe animals from around the world, participate in interactive exhibits, and learn about conservation efforts. The park offers a unique and immersive experience for animal lovers.', 'San Diego', ST_GeomFromText('POINT(33.0953 -116.0500)', 4326), '[\"zoo\", \"wildlife\", \"conservation\"]', 22, NOW()),\n"
        + //
        "  ('Santa Cruz Surfing Adventure', 'Catch some waves in Santa Cruz and experience the thrill of surfing. Whether you are a beginner or an experienced surfer, the beaches offer a variety of surf conditions. Take a lesson, rent equipment, and enjoy the surf culture of this iconic coastal city.', 'Santa Cruz', ST_GeomFromText('POINT(36.9741 -122.0308)', 4326), '[\"surfing\", \"beach\", \"adventure\"]', 23, NOW()),\n"
        + //
        "  ('Sacramento River Cruise', 'Enjoy a relaxing river cruise along the Sacramento River. Take in the scenic views of the city’s waterfront, learn about the area’s history, and enjoy a meal or drinks on board. The cruise offers a peaceful escape and a unique perspective of Sacramento’s landmarks.', 'Sacramento', ST_GeomFromText('POINT(38.5816 -121.4944)', 4326), '[\"cruise\", \"river\", \"sightseeing\"]', 24, NOW()),\n"
        + //
        "  ('Lake Tahoe Scenic Hike', 'Explore the scenic trails around Lake Tahoe. Enjoy stunning views of the lake, surrounding mountains, and alpine forests. The hike offers opportunities for wildlife spotting and relaxing in nature. Choose from a variety of trails to match your skill level and interests.', 'Lake Tahoe', ST_GeomFromText('POINT(39.0968 -120.0324)', 4326), '[\"hiking\", \"scenery\", \"nature\"]', 25, NOW()),\n"
        + //
        "  ('Big Bear Lake Outdoor Fun', 'Experience outdoor activities at Big Bear Lake, including hiking, boating, and fishing. The lake offers a range of recreational options, with beautiful mountain scenery and opportunities for adventure. Enjoy a day in the great outdoors with family and friends.', 'Big Bear Lake', ST_GeomFromText('POINT(34.2438 -116.9182)', 4326), '[\"lake\", \"outdoor\", \"recreation\"]', 26, NOW()),\n"
        + //
        "  ('Palm Springs Art Tour', 'Explore the vibrant art scene of Palm Springs on a guided tour. Visit local galleries, art installations, and public art displays. Learn about the region’s artistic heritage and discover the work of both established and emerging artists.', 'Palm Springs', ST_GeomFromText('POINT(33.8303 -116.5453)', 4326), '[\"art\", \"tour\", \"culture\"]', 27, NOW()),\n"
        + //
        "  ('Yosemite Photography Expedition', 'Capture the breathtaking beauty of Yosemite National Park on a photography expedition. Explore stunning vistas, iconic landmarks, and serene landscapes. Learn photography techniques and tips to capture the park’s natural splendor.', 'Yosemite', ST_GeomFromText('POINT(37.8651 -119.5383)', 4326), '[\"photography\", \"nature\", \"landscapes\"]', 28, NOW()),\n"
        + //
        "  ('Santa Barbara Beach Day', 'Relax and unwind on the beautiful beaches of Santa Barbara. Enjoy sunbathing, swimming, and beachside activities. Explore the nearby shops and restaurants, and take in the stunning coastal views. Santa Barbara’s beaches offer a perfect day of relaxation and fun.', 'Santa Barbara', ST_GeomFromText('POINT(34.4208 -119.6982)', 4326), '[\"beach\", \"relaxation\", \"scenery\"]', 29, NOW()),\n"
        + //
        "  ('San Jose Japantown Experience', 'Immerse yourself in the rich culture of San Jose’s Japantown. Visit local shops, dine at authentic Japanese restaurants, and explore cultural landmarks. Learn about the history and traditions of this vibrant community.', 'San Jose', ST_GeomFromText('POINT(37.3394 -121.8940)', 4326), '[\"culture\", \"Japantown\", \"food\"]', 30, NOW()),\n"
        + //
        "  ('Monterey Scenic Drive', 'Take a leisurely drive along the scenic routes of Monterey. Enjoy stunning coastal views, charming seaside towns, and picturesque landscapes. Stop at scenic overlooks and enjoy the serene beauty of the region.', 'Monterey', ST_GeomFromText('POINT(36.6002 -121.8947)', 4326), '[\"scenery\", \"drive\", \"coastal\"]', 31, NOW()),\n"
        + //
        "  ('Los Angeles Street Art Tour', 'Discover the vibrant street art scene of Los Angeles. Explore colorful murals, graffiti, and public art installations throughout the city. Learn about the artists and the stories behind their work, and see how street art shapes the urban landscape.', 'Los Angeles', ST_GeomFromText('POINT(34.0522 -118.2437)', 4326), '[\"street art\", \"culture\", \"tour\"]', 32, NOW()),\n"
        + //
        "  ('San Diego Historical Sites', 'Explore the historical sites of San Diego, including Old Town and Presidio Park. Learn about the city’s colonial past and its development over the years. Visit museums, historic buildings, and cultural landmarks to gain insight into San Diego’s history.', 'San Diego', ST_GeomFromText('POINT(32.7157 -117.1611)', 4326), '[\"history\", \"landmarks\", \"culture\"]', 33, NOW()),\n"
        + //
        "  ('Santa Cruz Boardwalk and Beach', 'Enjoy a fun-filled day at the Santa Cruz Boardwalk and Beach. Experience classic amusement park rides, play games, and relax on the sandy shores. The boardwalk offers a mix of entertainment, food, and beautiful beach views.', 'Santa Cruz', ST_GeomFromText('POINT(36.9741 -122.0308)', 4326), '[\"boardwalk\", \"amusement\", \"beach\"]', 34, NOW()),\n"
        + //
        "  ('Sacramento Arts Tour', 'Explore the vibrant arts scene of Sacramento. Visit local galleries, public art installations, and cultural centers. Discover the city’s artistic heritage and engage with its creative community through exhibitions and performances.', 'Sacramento', ST_GeomFromText('POINT(38.5816 -121.4944)', 4326), '[\"arts\", \"culture\", \"tour\"]', 35, NOW()),\n"
        + //
        "  ('Lake Tahoe Boat Cruise', 'Take a scenic boat cruise on Lake Tahoe and enjoy breathtaking views of the lake and surrounding mountains. Relax on deck, learn about the lake’s natural history, and experience the tranquil beauty of this alpine destination.', 'Lake Tahoe', ST_GeomFromText('POINT(39.0968 -120.0324)', 4326), '[\"cruise\", \"lake\", \"scenery\"]', 36, NOW()),\n"
        + //
        "  ('Big Bear Lake Fishing Trip', 'Experience a day of fishing at Big Bear Lake. Enjoy a peaceful day on the water, casting your line and relaxing amidst the beautiful lake scenery. Whether you’re an experienced angler or a beginner, Big Bear Lake offers great fishing opportunities.', 'Big Bear Lake', ST_GeomFromText('POINT(34.2438 -116.9182)', 4326), '[\"fishing\", \"lake\", \"relaxation\"]', 37, NOW()),\n"
        + //
        "  ('Palm Springs Hike and Dine', 'Hike the scenic trails of Palm Springs and enjoy a meal at a local restaurant. Experience stunning desert landscapes and savor the flavors of the region’s cuisine. This adventure combines outdoor exploration with culinary delights.', 'Palm Springs', ST_GeomFromText('POINT(33.8303 -116.5453)', 4326), '[\"hiking\", \"dining\", \"desert\"]', 38, NOW()),\n"
        + //
        "  ('Yosemite Rock Climbing', 'Take on the challenge of rock climbing in Yosemite National Park. Test your skills on world-famous climbing routes and enjoy spectacular views of the park’s granite cliffs and valleys. Climbing enthusiasts will find this an exhilarating adventure.', 'Yosemite', ST_GeomFromText('POINT(37.8651 -119.5383)', 4326), '[\"rock climbing\", \"adventure\", \"scenery\"]', 39, NOW()),\n"
        + //
        "  ('Santa Barbara Historic Tour', 'Explore the historic landmarks of Santa Barbara. Visit mission buildings, historic homes, and cultural institutions. Learn about the city’s rich history and architectural heritage while enjoying the beautiful coastal scenery.', 'Santa Barbara', ST_GeomFromText('POINT(34.4208 -119.6982)', 4326), '[\"history\", \"landmarks\", \"tour\"]', 40, NOW()),\n"
        + //
        "  ('San Jose Farm Visit', 'Visit a local farm in San Jose and experience farm life up close. Participate in farm activities, learn about sustainable farming practices, and enjoy fresh produce. The visit offers a unique glimpse into agriculture and local food production.', 'San Jose', ST_GeomFromText('POINT(37.3382 -121.8863)', 4326), '[\"farm\", \"sustainable\", \"fresh produce\"]', 41, NOW()),\n"
        + //
        "  ('Monterey Aquarium Adventure', 'Discover the wonders of marine life at the Monterey Bay Aquarium. Explore exhibits showcasing sea creatures from around the world, including jellyfish, sharks, and kelp forests. Engage in interactive displays and learn about ocean conservation.', 'Monterey', ST_GeomFromText('POINT(36.6002 -121.8947)', 4326), '[\"aquarium\", \"marine life\", \"conservation\"]', 42, NOW()),\n"
        + //
        "  ('Los Angeles Hollywood Tour', 'Explore the iconic Hollywood landmarks of Los Angeles. Visit the Hollywood Walk of Fame, TCL Chinese Theatre, and Hollywood Sign. Learn about the history of Hollywood and its impact on the entertainment industry.', 'Los Angeles', ST_GeomFromText('POINT(34.0522 -118.2437)', 4326), '[\"Hollywood\", \"landmarks\", \"entertainment\"]', 43, NOW()),\n"
        + //
        "  ('San Diego Harbor Cruise', 'Take a harbor cruise in San Diego and enjoy panoramic views of the city’s waterfront. Learn about the history and development of the harbor while taking in the sights of the skyline, naval ships, and the bay.', 'San Diego', ST_GeomFromText('POINT(32.7157 -117.1611)', 4326), '[\"harbor\", \"cruise\", \"views\"]', 44, NOW()),\n"
        + //
        "  ('Santa Cruz Vintage Shopping', 'Explore vintage shops in Santa Cruz and discover unique fashion finds. Browse through retro clothing, accessories, and antiques. Enjoy the quirky and eclectic atmosphere of Santa Cruz’s vintage scene.', 'Santa Cruz', ST_GeomFromText('POINT(36.9741 -122.0308)', 4326), '[\"vintage\", \"shopping\", \"fashion\"]', 45, NOW()),\n"
        + //
        "  ('Sacramento Historic Walking Tour', 'Embark on a historic walking tour of Sacramento. Explore the city’s historic districts, including Old Sacramento, and learn about its role in the Gold Rush era. The tour offers insights into Sacramento’s past and its development over time.', 'Sacramento', ST_GeomFromText('POINT(38.5816 -121.4944)', 4326), '[\"history\", \"walking tour\", \"Gold Rush\"]', 46, NOW()),\n"
        + //
        "  ('Lake Tahoe Kayaking', 'Enjoy a kayaking adventure on Lake Tahoe. Paddle across the pristine waters, explore hidden coves, and take in the stunning alpine scenery. Kayaking is a great way to experience the lake’s natural beauty and tranquility.', 'Lake Tahoe', ST_GeomFromText('POINT(39.0968 -120.0324)', 4326), '[\"kayaking\", \"lake\", \"adventure\"]', 47, NOW()),\n"
        + //
        "  ('Big Bear Lake Hiking Trail', 'Hike one of the scenic trails around Big Bear Lake. Enjoy panoramic views of the lake and surrounding mountains, and immerse yourself in nature. The trails offer various levels of difficulty, making it suitable for hikers of all abilities.', 'Big Bear Lake', ST_GeomFromText('POINT(34.2438 -116.9182)', 4326), '[\"hiking\", \"lake\", \"nature\"]', 48, NOW()),\n"
        + //
        "  ('Palm Springs Desert Safari', 'Experience a desert safari in Palm Springs. Ride through the arid landscapes in a 4x4 vehicle, explore the unique desert flora and fauna, and enjoy breathtaking desert views. The safari provides an adventurous way to discover the desert’s natural beauty.', 'Palm Springs', ST_GeomFromText('POINT(33.8303 -116.5453)', 4326), '[\"safari\", \"desert\", \"adventure\"]', 49, NOW()),\n"
        + //
        "  ('Yosemite Stargazing', 'Experience the magic of stargazing in Yosemite National Park. Observe the night sky from one of the park’s dark-sky areas, and learn about celestial objects and constellations. The stargazing experience offers a peaceful and awe-inspiring view of the universe.', 'Yosemite', ST_GeomFromText('POINT(37.8651 -119.5383)', 4326), '[\"stargazing\", \"nature\", \"night sky\"]', 50, NOW());\n";

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
