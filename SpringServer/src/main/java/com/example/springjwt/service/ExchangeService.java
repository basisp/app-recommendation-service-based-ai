package com.example.springjwt.service;

import com.example.springjwt.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final String flaskUrl = "http://13.209.104.1:5000/receive";  // Flask 서버 URL
    private final JWTUtil jwtUtil;  // Add this as a final field

    public Map<String, Object> sendToFlaskAndReceive(Map<String, Object> appsData, String authHeader) {
        // Extract username from JWT token
        String username = jwtUtil.getUsername(authHeader.replace("Bearer ", ""));

        // Create a new map to send to Flask
        Map<String, Object> requestPayload = new HashMap<>(appsData);

        // Add username to the payload
        requestPayload.put("username", username);

        RestTemplate restTemplate = new RestTemplate();

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "*/*");
        headers.set("Accept-Encoding","gzip, deflate, br");

        // HTTP 바디 설정 - apps 리스트와 username을 Flask로 전달
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestPayload, headers);

        // Flask 서버로 POST 요청 보내기
        ResponseEntity<Map> responseEntity = restTemplate.exchange(flaskUrl, HttpMethod.POST, requestEntity, Map.class);

        // 응답 데이터 반환
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();
        } else {
            throw new RuntimeException("Flask 서버 요청 실패");
        }
    }
}