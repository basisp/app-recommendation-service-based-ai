package com.example.springjwt.controller;


import com.example.springjwt.dto.AppDTO;
import com.example.springjwt.dto.ExchangeDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExchangeController {

    @PostMapping("/exchange")
    public ResponseEntity<String> exchangeApps(@RequestBody ExchangeDTO exchangeDTO) {
        StringBuilder response = new StringBuilder("Received apps:\n");

        for (AppDTO app : exchangeDTO.getApps()) {
            response.append("Package name: ")
                    .append(app.getPackageName())
                    .append("\n");
        }

        return ResponseEntity.ok(response.toString());
    }
}