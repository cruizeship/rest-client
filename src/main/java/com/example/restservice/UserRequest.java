package com.example.restservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {

  @JsonProperty("id")
  private Integer id;

  @JsonProperty("username")
  private String username;

  @JsonProperty("email")
  private String email;

  @JsonProperty("password")
  private String password;

  @JsonProperty("points")
  private Integer points;

  @JsonProperty("time")
  private String time;

  @JsonProperty("quests_created")
  private int[] quests_created;

  @JsonProperty("quests_completed")
  private int[] quests_completed;

  public UserRequest(Integer id, String username, String email, String password, Integer points, String time,
      int[] quests_created) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.password = password;
    this.points = points;
    this.time = time;
    this.quests_created = quests_created;
    this.quests_completed = quests_completed;
  }
}