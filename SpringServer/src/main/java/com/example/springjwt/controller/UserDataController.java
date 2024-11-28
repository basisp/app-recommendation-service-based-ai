package com.example.springjwt.controller;


import com.example.springjwt.dto.JoinDTO;
import com.example.springjwt.dto.JoinResponseDTO;
import com.example.springjwt.service.UserDataService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserDataController {

    private final UserDataService userDataService;
    @PostMapping("/save")

    public ResponseEntity<?> saveProcess(@RequestBody Map<String, Object> appsData) {

        userDataService.userDataSave(appsData);

        return ResponseEntity.ok("데이터 저장 완료!");
    }
}
