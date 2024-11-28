package com.example.springjwt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class JoinResponseDTO {
    private String message;
    private boolean success;
}