package com.example.springjwt.controller;


import com.example.springjwt.service.UserDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserDataController {

    private final UserDataService userDataService;

    @PostMapping("/save")
    public ResponseEntity<?> saveProcess(
            @RequestBody Map<String, Object> appsData,
            @RequestHeader(value = "access", required = false) String authHeader) {

        userDataService.userDataSave(appsData, authHeader);

        return ResponseEntity.ok("데이터 저장 완료!");
    }
}
