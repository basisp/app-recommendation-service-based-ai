package com.example.springjwt.service;

import com.example.springjwt.dto.JoinDTO;
import com.example.springjwt.dto.JoinResponseDTO;
import com.example.springjwt.entity.UserEntity;
import com.example.springjwt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JoinService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public ResponseEntity<JoinResponseDTO> joinProcess(JoinDTO joinDTO) {
        String username = joinDTO.getUsername();
        String password = joinDTO.getPassword();

        Boolean isExist = userRepository.existsByUsername(username);

        if (isExist) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new JoinResponseDTO("이미 존재하는 사용자명입니다. 다른 사용자명을 선택해주세요.", false));
        }


        UserEntity data = new UserEntity();
        data.setUsername(username);
        data.setPassword(bCryptPasswordEncoder.encode(password));
        data.setRole("ROLE_MEMBER");

        userRepository.save(data);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new JoinResponseDTO("회원가입이 성공적으로 완료되었습니다.", true));
    }
}
