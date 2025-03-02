package com.shop.frankit.service;

import com.shop.frankit.dto.auth.LoginRequest;
import com.shop.frankit.dto.auth.TokenResponse;
import com.shop.frankit.entity.RefreshToken;
import com.shop.frankit.exception.AuthException;
import com.shop.frankit.security.JwtTokenUtil;
import com.shop.frankit.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${jwt.access-token.expiration}")
    private Long accessTokenExpirationMs;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    /**
     * 사용자 로그인 및 토큰 발급
     */
    @Transactional
    public TokenResponse login(LoginRequest loginRequest) {
        try {
            // 사용자 인증
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            // 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 토큰 생성
            String accessToken = jwtTokenUtil.generateAccessToken(userDetails);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getEmail());

            log.info("로그인 성공: {}", userDetails.getEmail());

            // 토큰 응답 생성
            return new TokenResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                accessTokenExpirationMs / 1000
            );
        } catch (BadCredentialsException e) {
            log.error("로그인 실패 - 잘못된 인증 정보: {}", loginRequest.getEmail());
            throw AuthException.invalidCredentials();
        }
    }

    /**
     * 리프레시 토큰을 통한 액세스 토큰 갱신
     */
    @Transactional
    public TokenResponse refreshToken(String refreshToken) {
        return refreshTokenService.findByToken(refreshToken)
            .map(token -> {
                log.info("Refresh 토큰 찾음: {}", token.getEmail());
                return refreshTokenService.verifyExpiration(token);
            })
            .map(RefreshToken::getEmail)
            .map(email -> {
                UserDetailsImpl userDetails = (UserDetailsImpl) userService.loadUserByUsername(email);
                String accessToken = jwtTokenUtil.generateAccessToken(userDetails);
                log.info("새 Access 토큰 발급: {}", email);

                return new TokenResponse(
                    accessToken,
                    refreshToken,
                    "Bearer",
                    accessTokenExpirationMs / 1000);
            })
            .orElseThrow(() -> {
                log.error("Refresh 토큰 갱신 실패: 토큰을 찾을 수 없거나 만료됨");
                return AuthException.invalidToken();
            });
    }
}