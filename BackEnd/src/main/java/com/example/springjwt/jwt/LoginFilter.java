package com.example.springjwt.jwt;

import com.example.springjwt.dto.LoginDTO;
import com.example.springjwt.dto.LoginResponseDTO;
import com.example.springjwt.entity.RefreshEntity;
import com.example.springjwt.repository.RefreshRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private RefreshRepository refreshRepository;
    private final ObjectMapper objectMapper;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, RefreshRepository refreshRepository, ObjectMapper objectMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        LoginDTO loginDTO = new LoginDTO();
        try {
            loginDTO = objectMapper.readValue(request.getInputStream(), LoginDTO.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());
        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException {
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        String accessToken = jwtUtil.createJwt("access", username, role, 600000L);
        String refreshToken = jwtUtil.createJwt("refresh", username, role, 86400000L);

        addRefreshEntity(username, refreshToken, 86400000L);

        LoginResponseDTO loginResponse = new LoginResponseDTO(true, "Login successful");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("access", accessToken);
        response.addCookie(createCookie("refresh", refreshToken));
        response.setStatus(HttpStatus.OK.value());

        objectMapper.writeValue(response.getWriter(), loginResponse);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        LoginResponseDTO loginResponse = new LoginResponseDTO(false, "Login failed: " + failed.getMessage());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        objectMapper.writeValue(response.getWriter(), loginResponse);
    }

    private void addRefreshEntity(String username, String refresh, Long expiredMs) {
        Optional<RefreshEntity> existingRefresh = refreshRepository.findByUsername(username);
        existingRefresh.ifPresent(refreshRepository::delete);

        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setUsername(username);
        refreshEntity.setRefresh(refresh);
        refreshEntity.setExpiration(date.toString());

        refreshRepository.save(refreshEntity);
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        cookie.setHttpOnly(true);
        return cookie;
    }
}