package com.shop.frankit.controller;

import com.shop.frankit.dto.auth.LoginRequest;
import com.shop.frankit.dto.auth.RefreshTokenRequest;
import com.shop.frankit.dto.auth.TokenResponse;
import com.shop.frankit.dto.common.ApiResponse;
import com.shop.frankit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("로그인 시도: {}", loginRequest.getEmail());
        TokenResponse tokenResponse = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("로그인에 성공했습니다", tokenResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("토큰 갱신 요청 받음");
        TokenResponse tokenResponse = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("토큰이 갱신되었습니다", tokenResponse));
    }
}