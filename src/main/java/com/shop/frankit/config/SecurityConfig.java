package com.shop.frankit.config;

import com.shop.frankit.security.JwtAuthenticationEntryPoint;
import com.shop.frankit.security.JwtRequestFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtRequestFilter jwtRequestFilter) throws Exception {
        log.info("시큐리티 필터 체인 구성");

        http
            .csrf(csrf -> {
                csrf.disable();
                log.debug("CSRF 보호 비활성화");
            })
            .authorizeHttpRequests(auth -> {
                auth
                    .requestMatchers("/api/public/**", "/api/auth/**").permitAll()
                    .anyRequest().authenticated();
                log.debug("URL 기반 보안 규칙 설정 완료");
            })
            .exceptionHandling(ex -> {
                ex.authenticationEntryPoint(jwtAuthenticationEntryPoint);
                log.debug("인증 실패 처리기 설정");
            })
            .sessionManagement(session -> {
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                log.debug("세션 관리 정책: STATELESS");
            });

        // JWT 필터 추가
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        log.debug("JWT 요청 필터 추가됨");

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        log.debug("AuthenticationManager 빈 생성");
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.debug("BCryptPasswordEncoder 빈 생성");
        return new BCryptPasswordEncoder();
    }
}