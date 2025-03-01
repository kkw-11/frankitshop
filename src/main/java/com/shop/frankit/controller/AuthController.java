package com.shop.frankit.controller;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import com.shop.frankit.dto.UserDTO;
import com.shop.frankit.dto.auth.LoginRequest;
import com.shop.frankit.dto.auth.RefreshTokenRequest;
import com.shop.frankit.dto.auth.TokenResponse;
import com.shop.frankit.dto.common.ApiResponse;
import com.shop.frankit.entity.RefreshToken;
import com.shop.frankit.security.JwtTokenUtil;
import com.shop.frankit.security.UserDetailsImpl;
import com.shop.frankit.service.RefreshTokenService;
import com.shop.frankit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${jwt.access-token.expiration}")
    private Long accessTokenExpirationMs;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("로그인 시도: {}", loginRequest.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            String accessToken = jwtTokenUtil.generateAccessToken(userDetails);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getEmail());

            log.info("로그인 성공: {}", userDetails.getEmail());

            TokenResponse tokenResponse = new TokenResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                accessTokenExpirationMs / 1000
            );

            return ResponseEntity.ok(ApiResponse.success("로그인에 성공했습니다", tokenResponse));
        } catch (BadCredentialsException e) {
            log.error("로그인 실패 - 잘못된 인증 정보: {}", loginRequest.getEmail());
            return ResponseEntity.status(UNAUTHORIZED).body(
                ApiResponse.error("잘못된 이메일 또는 비밀번호입니다",
                    new ApiResponse.ErrorDetails("AUTH_001", "인증 실패"))
            );
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        log.info("토큰 갱신 요청 받음");

        return refreshTokenService.findByToken(requestRefreshToken)
            .map(token -> {
                log.info("Refresh 토큰 찾음: {}", token.getEmail());
                return refreshTokenService.verifyExpiration(token);
            })
            .map(RefreshToken::getEmail)
            .map(email -> {
                UserDetailsImpl userDetails = (UserDetailsImpl) userService.loadUserByUsername(email);
                String accessToken = jwtTokenUtil.generateAccessToken(userDetails);
                log.info("새 Access 토큰 발급: {}", email);

                TokenResponse tokenResponse = new TokenResponse(
                    accessToken,
                    requestRefreshToken,
                    "Bearer",
                    accessTokenExpirationMs / 1000);

                return ResponseEntity.ok(ApiResponse.success("토큰이 갱신되었습니다", tokenResponse));
            })
            .orElseGet(() -> {
                log.error("Refresh 토큰 갱신 실패: 토큰을 찾을 수 없거나 만료됨");
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("Refresh 토큰이 유효하지 않습니다",
                        new ApiResponse.ErrorDetails("AUTH_003", "유효하지 않은 토큰"))
                );
            });
    }
}
