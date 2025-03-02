package com.shop.frankit.service;

import com.shop.frankit.dto.auth.LoginRequest;
import com.shop.frankit.dto.auth.TokenResponse;
import com.shop.frankit.entity.RefreshToken;
import com.shop.frankit.entity.User;
import com.shop.frankit.exception.AuthException;
import com.shop.frankit.repository.RefreshTokenRepository;
import com.shop.frankit.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
@Slf4j
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String USER_EMAIL = "user@example.com";
    private static final String PASSWORD = "password";
    private static final String WRONG_PASSWORD = "wrongpassword";

    @BeforeEach
    void setUp() {
        log.info("테스트 데이터 초기화 시작");
        // 테스트 데이터 생성
        createTestUser(USER_EMAIL, PASSWORD, "USER");
        log.info("테스트 데이터 초기화 완료: 사용자 생성됨");
    }

    private void createTestUser(String email, String password, String role) {
        // 이미 존재하는 경우 중복 생성 방지
        if (!userRepository.existsByEmail(email)) {
            log.debug("사용자 생성: {}, 역할: {}", email, role);
            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            userRepository.save(user);
            log.debug("사용자 저장 완료: {}", email);
        } else {
            log.debug("사용자가 이미 존재함: {}", email);
        }
    }

    @Test
    @DisplayName("올바른 자격 증명으로 로그인 성공")
    void loginSuccess() {
        log.info("테스트 시작: 올바른 자격 증명으로 로그인 성공");

        // given
        log.debug("로그인 요청 객체 생성: {}", USER_EMAIL);
        LoginRequest loginRequest = new LoginRequest(USER_EMAIL, PASSWORD);

        // when
        log.debug("로그인 서비스 호출");
        TokenResponse response = authService.login(loginRequest);
        log.debug("로그인 응답 수신: 액세스 토큰 {}, 리프레시 토큰 {}",
            response.getAccessToken().substring(0, 10) + "...",
            response.getRefreshToken().substring(0, 10) + "...");

        // then
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertTrue(response.getExpiresIn() > 0);
        log.debug("응답 검증 완료: 토큰 타입 {}, 만료 시간 {}초", response.getTokenType(), response.getExpiresIn());

        log.info("테스트 완료: 올바른 자격 증명으로 로그인 성공");
    }

    @Test
    @DisplayName("잘못된 자격 증명으로 로그인 실패")
    void loginFailWithInvalidCredentials() {
        log.info("테스트 시작: 잘못된 자격 증명으로 로그인 실패");

        // given
        log.debug("잘못된 비밀번호로 로그인 요청 객체 생성: {}", USER_EMAIL);
        LoginRequest loginRequest = new LoginRequest(USER_EMAIL, WRONG_PASSWORD);

        // when & then
        log.debug("로그인 서비스 호출 - 예외 발생 예상");
        assertThrows(AuthException.class, () -> {
            authService.login(loginRequest);
        });
        log.debug("예상대로 AuthException 발생 확인");

        log.info("테스트 완료: 잘못된 자격 증명으로 로그인 실패");
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로 새 액세스 토큰 발급")
    void refreshTokenSuccess() {
        log.info("테스트 시작: 유효한 리프레시 토큰으로 새 액세스 토큰 발급");

        // given: 사용자와 리프레시 토큰을 직접 생성
        log.debug("테스트 사용자 조회: {}", USER_EMAIL);
        User user = userRepository.findByEmail(USER_EMAIL).orElseThrow();

        String tokenValue = "test-refresh-token-" + System.nanoTime();
        log.debug("테스트용 리프레시 토큰 직접 생성: {}", tokenValue);

        RefreshToken directRefreshToken = new RefreshToken();
        directRefreshToken.setEmail(user.getEmail());
        directRefreshToken.setToken(tokenValue);
        directRefreshToken.setExpiryDate(Instant.now().plusSeconds(86400)); // 24시간
        refreshTokenRepository.save(directRefreshToken);
        log.debug("리프레시 토큰 저장 완료, 만료시간: {}", directRefreshToken.getExpiryDate());

        // when: 리프레시 토큰으로 액세스 토큰 갱신
        log.debug("토큰 갱신 서비스 호출");
        TokenResponse refreshResponse = authService.refreshToken(directRefreshToken.getToken());
        log.debug("토큰 갱신 응답 수신: 새 액세스 토큰 {}", refreshResponse.getAccessToken().substring(0, 10) + "...");

        // then
        assertNotNull(refreshResponse);
        assertNotNull(refreshResponse.getAccessToken());
        assertEquals(directRefreshToken.getToken(), refreshResponse.getRefreshToken());
        log.debug("응답 검증 완료: 리프레시 토큰이 동일함을 확인");

        log.info("테스트 완료: 유효한 리프레시 토큰으로 새 액세스 토큰 발급");
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰으로 토큰 갱신 실패")
    void refreshTokenFailWithInvalidToken() {
        log.info("테스트 시작: 유효하지 않은 리프레시 토큰으로 토큰 갱신 실패");

        // given
        String invalidToken = "invalid-refresh-token";
        log.debug("유효하지 않은 리프레시 토큰 준비: {}", invalidToken);

        // when & then
        log.debug("유효하지 않은 토큰으로 갱신 시도 - 예외 발생 예상");
        assertThrows(AuthException.class, () -> {
            authService.refreshToken(invalidToken);
        });
        log.debug("예상대로 AuthException 발생 확인");

        log.info("테스트 완료: 유효하지 않은 리프레시 토큰으로 토큰 갱신 실패");
    }
}