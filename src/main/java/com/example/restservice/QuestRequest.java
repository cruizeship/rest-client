package com.example.restservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class QuestRequest {

  @JsonProperty("latitude")
  private Double latitude;

  @JsonProperty("longitude")
  private Double longitude;

  @JsonProperty("radius")
  private Double radius;

  @JsonProperty("id")
  private Integer id;

  @JsonProperty("creator_id")
  private Integer creatorId;

  @JsonProperty("title")
  private String title;

  @JsonProperty("description")
  private String description;

  @JsonProperty("city")
  private String city;

  @JsonProperty("time")
  private String time;

  @JsonProperty("difficulty")
  private Integer difficulty;

  @JsonProperty("time_needed")
  private Double timeNeeded;

  @JsonProperty("search_query")
  private String searchQuery;

  @JsonProperty("search_filters")
  private List<Map<String, Object>> searchFilters;

}