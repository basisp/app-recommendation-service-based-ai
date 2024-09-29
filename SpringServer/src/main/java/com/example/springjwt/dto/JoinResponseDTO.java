package com.example.springjwt.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinResponseDTO {
    private String message;
    private boolean success;

    public JoinResponseDTO(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

}