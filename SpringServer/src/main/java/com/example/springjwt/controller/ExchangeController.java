package com.example.springjwt.controller;

import com.example.springjwt.dto.AppDTO;
import com.example.springjwt.dto.AppListDTO;
import com.example.springjwt.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeService exchangeService;

    @PostMapping("/exchange")
    public ResponseEntity<Map<String, Object>> exchangeApps(@RequestBody Map<String, Object> appsData) {


        // Flask 서버로 전송하고 응답 받기
        Map<String, Object> flaskResponse = exchangeService.sendToFlaskAndReceive(appsData);


        return ResponseEntity.ok(flaskResponse);
    }
}
