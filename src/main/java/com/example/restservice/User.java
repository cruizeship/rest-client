package com.example.restservice;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {

  private int id;
  private String username;
  private String email;
  private String passwordHash;
  private int points;
  private int[] quests;
  private String createdAt;
}