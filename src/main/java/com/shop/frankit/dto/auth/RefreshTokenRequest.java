package com.shop.frankit.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * RefreshTokenRequest는 리프레시 토큰을 이용해 새로운 액세스 토큰을 요청할 때 사용
 */
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequest {
    private String refreshToken;
}
