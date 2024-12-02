package com.example.springjwt.controller;

import com.example.springjwt.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeService exchangeService;

    @PostMapping("/exchange")
    public ResponseEntity<Map<String, Object>> exchangeApps(
            @RequestBody Map<String, Object> appsData,
            @RequestHeader(value = "access", required = false) String authHeader) {

        // Flask 서버로 전송하고 응답 받기
        Map<String, Object> flaskResponse = exchangeService.sendToFlaskAndReceive(appsData, authHeader);

        return ResponseEntity.ok(flaskResponse);
    }
}
