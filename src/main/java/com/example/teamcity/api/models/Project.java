package com.example.teamcity.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {
    @JsonProperty("parentProjectId")
    private String parentProjectId;

    @JsonProperty("id")
    private String id;

    @JsonProperty("locator")
    private String locator;

    @JsonProperty("name")
    private String name;
}
