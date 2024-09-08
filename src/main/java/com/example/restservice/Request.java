package com.example.restservice;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Request {

  @JsonProperty("coordinates")
  private double[] coordinates;

  @JsonProperty("radius")
  private double radius;

  @JsonProperty("id")
  private int id;

  @JsonProperty("creator_id")
  private int creator_id;

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

  public double[] getCoordinates() {
    return coordinates;
  }

  public void setCoordinates(double[] coordinates) {
    this.coordinates = coordinates;
  }

  public double getRadius() {
    return radius;
  }

  public void setRadius(double radius) {
    this.radius = radius;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getCreatorId() {
    return creator_id;
  }

  public void setCreatorId(int creator_id) {
    this.creator_id = creator_id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return title;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public String[] getTags() {
    return tags;
  }

  public void setTags(String[] tags) {
    this.tags = tags;
  }
}
