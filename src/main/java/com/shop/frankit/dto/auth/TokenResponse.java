package com.shop.frankit.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * TokenResponse는 로그인 성공 시 반환되는 토큰 정보를 담는 객체
 */
@Getter @Setter
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
}
