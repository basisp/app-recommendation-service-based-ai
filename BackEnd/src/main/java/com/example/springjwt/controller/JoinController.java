// JoinController.java
package com.example.springjwt.controller;

import com.example.springjwt.dto.JoinDTO;
import com.example.springjwt.dto.JoinResponseDTO;
import com.example.springjwt.service.JoinService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JoinController {

    private final JoinService joinService;

    public JoinController(JoinService joinService) {
        this.joinService = joinService;
    }

    @PostMapping("/join")
    public ResponseEntity<JoinResponseDTO> joinProcess(@RequestBody JoinDTO joinDTO) {
        return joinService.joinProcess(joinDTO);
    }
}