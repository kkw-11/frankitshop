package com.shop.frankit.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtRequestFilter는 모든 HTTP 요청을 가로채서 Authorization 헤더의 JWT 토큰을 검증하는 필터
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {

        try {
            String token = extractJwtFromRequest(request);
            if (token != null) {
                processToken(token, request);
            }
        } catch (Exception e) {
            log.error("JWT 토큰 처리 중 오류 발생: {}", e.getMessage());
            // 예외를 로깅만 하고 다음 필터로 진행
            // 인증 실패 처리는 JwtAuthenticationEntryPoint에서 수행
        }

        chain.doFilter(request, response);
    }

    /**
     * HTTP 요청에서 JWT 토큰 추출
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * JWT 토큰 처리 및 인증 설정
     */
    private void processToken(String token, HttpServletRequest request) {
        // Access Token만 검증
        if (!jwtTokenUtil.isAccessToken(token)) {
            log.debug("Access 토큰이 아님, 인증 건너뜀");
            return;
        }

        String email = jwtTokenUtil.extractUsername(token);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (jwtTokenUtil.validateToken(token, userDetails)) {
                log.debug("유효한 JWT 토큰, 사용자 인증: {}", email);
                setAuthentication(userDetails, request);
            } else {
                log.warn("유효하지 않은 JWT 토큰: {}", email);
            }
        }
    }

    /**
     * SecurityContext에 인증 정보 설정
     */
    private void setAuthentication(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}