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
    private Integer creatorId;

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

    @JsonProperty("search_query")
    private String searchQuery;

    @JsonProperty("sort_time_needed")
    private Boolean sortTimeNeeded;

    @JsonProperty("time_weight")
    private Double timeWeight;

    @JsonProperty("sort_difficulty")
    private Boolean sortDifficulty;

    @JsonProperty("difficulty_weight")
    private Double difficultyWeight;

    @JsonProperty("sort_popularity")
    private Boolean sortPopularity;

    @JsonProperty("popularity_weight")
    private Double popularityWeight;

    @JsonProperty("use_similarity_weight")
    private Boolean useSimilarityWeight;

    @JsonProperty("similarity_weight")
    private Double similarityWeight;

    }
