package com.shop.frankit.exception;

public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_001");
    }

    public static ResourceNotFoundException userNotFound(String email) {
        return new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + email);
    }
}
