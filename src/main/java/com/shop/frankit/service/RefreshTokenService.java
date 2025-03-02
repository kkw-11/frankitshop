package com.shop.frankit.service;

import com.shop.frankit.entity.RefreshToken;
import com.shop.frankit.exception.AuthException;
import com.shop.frankit.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 토큰 값으로 RefreshToken 조회
     */
    public Optional<RefreshToken> findByToken(String token) {
        log.debug("토큰으로 RefreshToken 조회: {}", token.substring(0, Math.min(10, token.length())) + "...");
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * 이메일로 RefreshToken 조회
     */
    public Optional<RefreshToken> findByEmail(String email) {
        log.debug("이메일로 RefreshToken 조회: {}", email);
        return refreshTokenRepository.findByEmail(email);
    }

    /**
     * 새 RefreshToken 생성
     */
    @Transactional
    public RefreshToken createRefreshToken(String email) {
        log.info("새 Refresh 토큰 생성: {}", email);

        // 기존 토큰이 있으면 삭제
        refreshTokenRepository.findByEmail(email)
            .ifPresent(token -> {
                log.info("기존 Refresh 토큰 삭제: {}", email);
                refreshTokenRepository.delete(token);
            });

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setEmail(email);

        // 개선된 토큰 생성 방식 - UUID + nanoTime으로 고유성 강화
        String token;
        boolean isDuplicate;
        do {
            token = UUID.randomUUID().toString() + "-" + System.nanoTime();
            isDuplicate = refreshTokenRepository.existsByToken(token);
            if (isDuplicate) {
                log.warn("토큰 중복 발생, 재생성 시도");
            }
        } while (isDuplicate);

        refreshToken.setToken(token);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.info("Refresh 토큰 저장 완료: {}", email);

        return savedToken;
    }

    /**
     * RefreshToken 만료 검증
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        log.info("Refresh 토큰 만료 확인: {}", token.getEmail());

        if (token.isExpired()) {
            log.warn("만료된 Refresh 토큰: {}", token.getEmail());
            refreshTokenRepository.delete(token);
            throw new AuthException("Refresh 토큰이 만료되었습니다. 다시 로그인해주세요", "AUTH_002");
        }

        log.info("유효한 Refresh 토큰: {}", token.getEmail());
        return token;
    }

    /**
     * 이메일로 RefreshToken 삭제
     */
    @Transactional
    public void deleteByEmail(String email) {
        log.info("Refresh 토큰 삭제: {}", email);
        refreshTokenRepository.deleteByEmail(email);
    }
}