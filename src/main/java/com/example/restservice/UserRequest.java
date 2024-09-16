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

  @JsonProperty("quests")
  private int[] quests;

  public UserRequest(Integer id, String username, String email, String password, Integer points, String time,
      int[] quests) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.password = password;
    this.points = points;
    this.time = time;
    this.quests = quests;
  }
}