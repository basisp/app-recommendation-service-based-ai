package com.example.springjwt.service;

import com.example.springjwt.entity.UserDataEntity;
import com.example.springjwt.jwt.JWTUtil;
import com.example.springjwt.repository.UserDataRepository;
import com.example.springjwt.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserDataService {

    @Autowired
    private final UserDataRepository userDataRepository; // JPA 리포지토리
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    public void userDataSave(Map<String, Object> appData, String authHeader) {

        String username = jwtUtil.getUsername(authHeader.replace("Bearer ", ""));
        String allAppJson =null;
        ObjectMapper objectMapper = new ObjectMapper();

        // UserEntity에 같은 username이 없으면 예외 발생
        userRepository.findByUsername(username).orElseThrow(() ->
                new RuntimeException("Username " + username + " not found.")
        );

        try {
            // Map을 JSON String으로 변환
            allAppJson = objectMapper.writeValueAsString(appData);
        } catch (Exception e) {
            e.printStackTrace(); // 변환 중 에러 발생 시 처리
        }

        //같은 이름 있으면 데이터 삭제
        userDataRepository.findByUsername(username).ifPresent(userDataRepository::delete);

        // UserDataEntity 객체 생성 및 저장
        UserDataEntity userDataEntity = new UserDataEntity();
        userDataEntity.setUsername(username);
        userDataEntity.setAllAppJson(allAppJson);

        userDataRepository.save(userDataEntity);
    }

}




