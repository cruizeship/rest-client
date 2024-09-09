package com.example.restservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Request {

  @JsonProperty("coordinates")
  private double[] coordinates;

  @JsonProperty("radius")
  private Double radius;

  @JsonProperty("id")
  private Integer id;

  @JsonProperty("creator_id")
  private Integer creator_id;

  @JsonProperty("title")
  private String title;

  @JsonProperty("description")
  private String description;

  @JsonProperty("city")
  private String city;

  @JsonProperty("time")
  private String time;

  @JsonProperty("tags")
  private String[] tags;

}