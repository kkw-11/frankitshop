package com.shop.frankit.service;

import com.shop.frankit.dto.UserDTO;
import com.shop.frankit.entity.User;
import com.shop.frankit.exception.ResourceNotFoundException;
import com.shop.frankit.repository.UserRepository;
import com.shop.frankit.security.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
@Slf4j
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String USER_EMAIL = "user@example.com";
    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String PASSWORD = "password";

    @BeforeEach
    void setUp() {
        log.info("테스트 데이터 초기화 시작");
        // 테스트 데이터 생성
        createTestUser(USER_EMAIL, PASSWORD, "USER");
        createTestUser(ADMIN_EMAIL, PASSWORD, "ADMIN");
        log.info("테스트 데이터 초기화 완료: 일반 사용자 및 관리자 생성됨");
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
    @DisplayName("이메일로 사용자 상세 정보 로드 성공")
    void loadUserByUsernameSuccess() {
        log.info("테스트 시작: 이메일로 사용자 상세 정보 로드 성공");

        // when
        log.debug("사용자 상세 정보 로드: {}", USER_EMAIL);
        UserDetails userDetails = userService.loadUserByUsername(USER_EMAIL);
        log.debug("사용자 상세 정보 로드 완료: {}", userDetails.getUsername());

        // then
        assertNotNull(userDetails);
        assertEquals(USER_EMAIL, userDetails.getUsername());
        assertTrue(userDetails instanceof UserDetailsImpl);

        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
        assertEquals("USER", userDetailsImpl.getRole());
        log.debug("사용자 상세 정보 검증 완료: 역할 {}", userDetailsImpl.getRole());

        log.info("테스트 완료: 이메일로 사용자 상세 정보 로드 성공");
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 예외 발생")
    void loadUserByUsernameNotFound() {
        log.info("테스트 시작: 존재하지 않는 사용자 조회 시 예외 발생");

        // given
        String nonExistentEmail = "nonexistent@example.com";
        log.debug("존재하지 않는 이메일로 테스트: {}", nonExistentEmail);

        // when & then
        log.debug("존재하지 않는 사용자 조회 시도 - 예외 발생 예상");
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername(nonExistentEmail);
        });
        log.debug("예상대로 UsernameNotFoundException 발생 확인");

        log.info("테스트 완료: 존재하지 않는 사용자 조회 시 예외 발생");
    }

    @Test
    @DisplayName("이메일로 사용자 DTO 조회 성공")
    void getUserByEmailSuccess() {
        log.info("테스트 시작: 이메일로 사용자 DTO 조회 성공");

        // when
        log.debug("사용자 DTO 조회: {}", ADMIN_EMAIL);
        UserDTO userDTO = userService.getUserByEmail(ADMIN_EMAIL);
        log.debug("사용자 DTO 조회 완료: {}", userDTO.getUsername());

        // then
        assertNotNull(userDTO);
        assertEquals(ADMIN_EMAIL, userDTO.getUsername());
        assertEquals("ADMIN", userDTO.getRole());
        log.debug("사용자 DTO 검증 완료: 역할 {}", userDTO.getRole());

        log.info("테스트 완료: 이메일로 사용자 DTO 조회 성공");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 DTO 조회 시 예외 발생")
    void getUserByEmailNotFound() {
        log.info("테스트 시작: 존재하지 않는 이메일로 DTO 조회 시 예외 발생");

        // given
        String nonExistentEmail = "nonexistent@example.com";
        log.debug("존재하지 않는 이메일로 테스트: {}", nonExistentEmail);

        // when & then
        log.debug("존재하지 않는 이메일로 DTO 조회 시도 - 예외 발생 예상");
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserByEmail(nonExistentEmail);
        });
        log.debug("예상대로 ResourceNotFoundException 발생 확인");

        log.info("테스트 완료: 존재하지 않는 이메일로 DTO 조회 시 예외 발생");
    }
}