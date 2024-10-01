package com.example.springjwt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppDTO {
    private Long timestamp;

    @JsonProperty("package_name")
    private String packageName;

    private String category;
}
