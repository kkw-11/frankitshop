package com.shop.frankit.service;

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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
@Slf4j
public class RefreshTokenServiceTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String PASSWORD = "password";

    @BeforeEach
    void setUp() {
        log.info("테스트 데이터 초기화 시작");
        // 테스트 실행 전 기본 사용자 생성
        createTestUser(TEST_EMAIL, PASSWORD, "USER");
        log.info("테스트 데이터 초기화 완료: 기본 사용자 생성됨");
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
    @DisplayName("새 리프레시 토큰 생성 성공")
    void createRefreshTokenSuccess() {
        log.info("테스트 시작: 새 리프레시 토큰 생성 성공");

        // when
        log.debug("리프레시 토큰 생성 시도: {}", TEST_EMAIL);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(TEST_EMAIL);
        log.debug("리프레시 토큰 생성됨: {}, 만료시간: {}", refreshToken.getToken().substring(0, 10) + "...", refreshToken.getExpiryDate());

        // then
        assertNotNull(refreshToken);
        assertEquals(TEST_EMAIL, refreshToken.getEmail());
        assertNotNull(refreshToken.getToken());
        assertTrue(refreshToken.getExpiryDate().isAfter(Instant.now()));

        // 데이터베이스에 저장되었는지 확인
        log.debug("데이터베이스에서 토큰 조회: {}", TEST_EMAIL);
        Optional<RefreshToken> savedToken = refreshTokenRepository.findByEmail(TEST_EMAIL);
        assertTrue(savedToken.isPresent());
        assertEquals(refreshToken.getToken(), savedToken.get().getToken());
        log.info("테스트 완료: 새 리프레시 토큰 생성 성공");
    }

    @Test
    @DisplayName("동일한 이메일로 토큰 생성 시 기존 토큰 대체")
    void createRefreshTokenReplacesExisting() {
        log.info("테스트 시작: 동일한 이메일로 토큰 생성 시 기존 토큰 대체");

        // given - 첫 번째 토큰 생성
        String email = "replace@example.com";
        log.debug("테스트 사용자 생성: {}", email);
        createTestUser(email, PASSWORD, "USER");

        log.debug("첫 번째 리프레시 토큰 생성: {}", email);
        RefreshToken firstToken = refreshTokenService.createRefreshToken(email);
        log.debug("첫 번째 토큰 생성됨: {}", firstToken.getToken().substring(0, 10) + "...");

        // when - 같은 이메일로 두 번째 토큰 생성
        log.debug("두 번째 리프레시 토큰 생성 시도: {}", email);
        RefreshToken secondToken = refreshTokenService.createRefreshToken(email);
        log.debug("두 번째 토큰 생성됨: {}", secondToken.getToken().substring(0, 10) + "...");

        // then
        assertNotEquals(firstToken.getToken(), secondToken.getToken());

        // 기존 토큰은 조회되지 않아야 함
        log.debug("첫 번째 토큰 조회 시도");
        Optional<RefreshToken> foundFirstToken = refreshTokenService.findByToken(firstToken.getToken());
        assertTrue(foundFirstToken.isEmpty());
        log.debug("첫 번째 토큰이 삭제되었음을 확인");

        // 새 토큰은 조회되어야 함
        log.debug("두 번째 토큰 조회 시도");
        Optional<RefreshToken> foundSecondToken = refreshTokenService.findByToken(secondToken.getToken());
        assertTrue(foundSecondToken.isPresent());
        log.debug("두 번째 토큰이 존재함을 확인");

        log.info("테스트 완료: 동일한 이메일로 토큰 생성 시 기존 토큰 대체");
    }

    @Test
    @DisplayName("토큰으로 리프레시 토큰 찾기 성공")
    void findByTokenSuccess() {
        log.info("테스트 시작: 토큰으로 리프레시 토큰 찾기 성공");

        // given - 직접 토큰 생성 및 저장
        String email = "find@example.com";
        log.debug("테스트 사용자 생성: {}", email);
        createTestUser(email, PASSWORD, "USER");

        String tokenValue = UUID.randomUUID().toString();
        log.debug("테스트용 리프레시 토큰 직접 생성: {}", tokenValue);

        RefreshToken token = new RefreshToken();
        token.setEmail(email);
        token.setToken(tokenValue);
        token.setExpiryDate(Instant.now().plusSeconds(86400));
        refreshTokenRepository.save(token);
        log.debug("리프레시 토큰 저장 완료");

        // when
        log.debug("토큰으로 리프레시 토큰 조회 시도: {}", tokenValue);
        Optional<RefreshToken> foundToken = refreshTokenService.findByToken(token.getToken());

        // then
        assertTrue(foundToken.isPresent());
        assertEquals(email, foundToken.get().getEmail());
        assertEquals(token.getToken(), foundToken.get().getToken());
        log.debug("토큰이 성공적으로 조회됨: {}, 이메일: {}", tokenValue, email);

        log.info("테스트 완료: 토큰으로 리프레시 토큰 찾기 성공");
    }

    @Test
    @DisplayName("이메일로 리프레시 토큰 찾기 성공")
    void findByEmailSuccess() {
        log.info("테스트 시작: 이메일로 리프레시 토큰 찾기 성공");

        // given - 직접 토큰 생성 및 저장
        String email = "findemail@example.com";
        log.debug("테스트 사용자 생성: {}", email);
        createTestUser(email, PASSWORD, "USER");

        String tokenValue = UUID.randomUUID().toString();
        log.debug("테스트용 리프레시 토큰 직접 생성: {}", tokenValue);

        RefreshToken token = new RefreshToken();
        token.setEmail(email);
        token.setToken(tokenValue);
        token.setExpiryDate(Instant.now().plusSeconds(86400));
        refreshTokenRepository.save(token);
        log.debug("리프레시 토큰 저장 완료");

        // when
        log.debug("이메일로 리프레시 토큰 조회 시도: {}", email);
        Optional<RefreshToken> foundToken = refreshTokenService.findByEmail(email);

        // then
        assertTrue(foundToken.isPresent());
        assertEquals(email, foundToken.get().getEmail());
        assertEquals(token.getToken(), foundToken.get().getToken());
        log.debug("토큰이 성공적으로 조회됨: {}, 이메일: {}", foundToken.get().getToken(), email);

        log.info("테스트 완료: 이메일로 리프레시 토큰 찾기 성공");
    }

    @Test
    @DisplayName("만료된 토큰 검증 시 예외 발생")
    void verifyExpirationFailsForExpiredToken() {
        log.info("테스트 시작: 만료된 토큰 검증 시 예외 발생");

        // given - 만료된 토큰 생성
        String email = "expired@example.com";
        log.debug("테스트 사용자 생성: {}", email);
        createTestUser(email, PASSWORD, "USER");

        String tokenValue = "expired-token-" + System.nanoTime();
        log.debug("만료된 테스트용 리프레시 토큰 직접 생성: {}", tokenValue);

        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setEmail(email);
        expiredToken.setToken(tokenValue);
        expiredToken.setExpiryDate(Instant.now().minusSeconds(3600)); // 1시간 전 만료
        refreshTokenRepository.save(expiredToken);
        log.debug("만료된 리프레시 토큰 저장 완료, 만료시간: {}", expiredToken.getExpiryDate());

        // when & then
        log.debug("만료된 토큰 검증 시도 - 예외 발생 예상");
        assertThrows(AuthException.class, () -> {
            refreshTokenService.verifyExpiration(expiredToken);
        });
        log.debug("예상대로 예외 발생");

        // 만료된 토큰은 삭제되어야 함
        log.debug("만료된 토큰 삭제 확인");
        Optional<RefreshToken> deletedToken = refreshTokenRepository.findByToken(expiredToken.getToken());
        assertTrue(deletedToken.isEmpty());
        log.debug("만료된 토큰이 삭제되었음을 확인");

        log.info("테스트 완료: 만료된 토큰 검증 시 예외 발생");
    }

    @Test
    @DisplayName("유효한 토큰 검증 성공")
    void verifyExpirationSucceedsForValidToken() {
        log.info("테스트 시작: 유효한 토큰 검증 성공");

        // given - 유효한 토큰 생성
        String email = "valid@example.com";
        log.debug("테스트 사용자 생성: {}", email);
        createTestUser(email, PASSWORD, "USER");

        String tokenValue = "valid-token-" + System.nanoTime();
        log.debug("유효한 테스트용 리프레시 토큰 직접 생성: {}", tokenValue);

        RefreshToken validToken = new RefreshToken();
        validToken.setEmail(email);
        validToken.setToken(tokenValue);
        validToken.setExpiryDate(Instant.now().plusSeconds(3600)); // 1시간 후 만료
        refreshTokenRepository.save(validToken);
        log.debug("유효한 리프레시 토큰 저장 완료, 만료시간: {}", validToken.getExpiryDate());

        // when
        log.debug("유효한 토큰 검증 시도");
        RefreshToken verifiedToken = refreshTokenService.verifyExpiration(validToken);

        // then
        assertNotNull(verifiedToken);
        assertEquals(validToken.getToken(), verifiedToken.getToken());
        log.debug("토큰이 성공적으로 검증됨: {}", verifiedToken.getToken());

        log.info("테스트 완료: 유효한 토큰 검증 성공");
    }

    @Test
    @DisplayName("이메일로 토큰 삭제 성공")
    void deleteByEmailSuccess() {
        log.info("테스트 시작: 이메일로 토큰 삭제 성공");

        // given - 토큰 생성
        String email = "delete@example.com";
        log.debug("테스트 사용자 생성: {}", email);
        createTestUser(email, PASSWORD, "USER");

        String tokenValue = UUID.randomUUID().toString();
        log.debug("테스트용 리프레시 토큰 직접 생성: {}", tokenValue);

        RefreshToken token = new RefreshToken();
        token.setEmail(email);
        token.setToken(tokenValue);
        token.setExpiryDate(Instant.now().plusSeconds(86400));
        refreshTokenRepository.save(token);
        log.debug("리프레시 토큰 저장 완료");

        // 토큰이 저장되었는지 확인
        assertTrue(refreshTokenRepository.findByEmail(email).isPresent());
        log.debug("토큰이 성공적으로 저장되었음을 확인");

        // when - 이메일로 토큰 삭제
        log.debug("이메일로 토큰 삭제 시도: {}", email);
        refreshTokenService.deleteByEmail(email);

        // then - 토큰이 삭제되었는지 확인
        log.debug("토큰 삭제 확인");
        Optional<RefreshToken> foundToken = refreshTokenService.findByEmail(email);
        assertTrue(foundToken.isEmpty());
        log.debug("토큰이 성공적으로 삭제되었음을 확인");

        log.info("테스트 완료: 이메일로 토큰 삭제 성공");
    }
}