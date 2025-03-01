package com.shop.frankit.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.frankit.dto.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JwtAuthenticationEntryPoint는 인증되지 않은 사용자가 보호된 리소스에 접근할 때 호출되는 클래스
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException {

        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
            "AUTH_001", authException.getMessage());
        ApiResponse<?> apiResponse = ApiResponse.error("인증에 실패했습니다", errorDetails);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}