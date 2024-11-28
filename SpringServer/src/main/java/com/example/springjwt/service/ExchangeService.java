package com.example.springjwt.service;

import com.example.springjwt.dto.AppDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Map;

@Service
public class ExchangeService {



    private final String flaskUrl = "http://13.209.104.1:5000/receive";  // Flask 서버 URL

    public Map<String, Object> sendToFlaskAndReceive(Map<String, Object> appsData) {
        RestTemplate restTemplate = new RestTemplate();

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "*/*");
        headers.set("Accept-Encoding","gzip, deflate, br");

        // HTTP 바디 설정 - apps 리스트를 그대로 Flask로 전달
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(appsData, headers);

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
