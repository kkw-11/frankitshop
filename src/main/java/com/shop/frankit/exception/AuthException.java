package com.shop.frankit.exception;

public class AuthException extends AppException {
    public AuthException(String message, String errorCode) {
        super(message, errorCode);
    }

    public static AuthException invalidCredentials() {
        return new AuthException("잘못된 인증 정보입니다", "AUTH_001");
    }

    public static AuthException tokenExpired() {
        return new AuthException("토큰이 만료되었습니다", "AUTH_002");
    }

    public static AuthException invalidToken() {
        return new AuthException("유효하지 않은 토큰입니다", "AUTH_003");
    }

    public static AuthException accessDenied() {
        return new AuthException("접근 권한이 없습니다", "AUTH_004");
    }
}
