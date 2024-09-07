package com.example.restservice;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Request {

  @JsonProperty("latitude")
  private double latitude;

  @JsonProperty("longitude")
  private double longitude;

  @JsonProperty("radius")
  private double radius;

  @JsonProperty("id")
  private int id;

  @JsonProperty("creator_id")
  private int creator_id;

  // Getters and setters
  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
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
}
