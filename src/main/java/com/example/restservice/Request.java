package com.example.restservice;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Request {

  @JsonProperty("coordinates")
  private double[] coordinates; // Array to hold latitude and longitude

  @JsonProperty("radius")
  private double radius;

  @JsonProperty("id")
  private int id;

  @JsonProperty("creator_id")
  private int creator_id;

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
}
