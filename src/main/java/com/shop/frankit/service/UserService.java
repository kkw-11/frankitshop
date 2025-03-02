package com.shop.frankit.service;

import com.shop.frankit.dto.UserDTO;
import com.shop.frankit.entity.User;
import com.shop.frankit.exception.ResourceNotFoundException;
import com.shop.frankit.repository.UserRepository;
import com.shop.frankit.security.UserDetailsImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 테스트용 초기 사용자 등록
     * 애플리케이션 시작 시 샘플 사용자 생성
     */
    @PostConstruct
    public void init() {
        createTestUserIfNotExist("user@example.com", "password", "USER");
        createTestUserIfNotExist("admin@example.com", "password", "ADMIN");
    }

    /**
     * 테스트 사용자 생성 헬퍼 메소드
     */
    private void createTestUserIfNotExist(String email, String password, String role) {
        if (!userRepository.existsByEmail(email)) {
            log.info("테스트 사용자 생성: {}, 역할: {}", email, role);
            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            userRepository.save(user);
        }
    }

    /**
     * Spring Security에서 사용하는 사용자 조회 메소드
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("이메일로 사용자 조회: {}", email);
        return userRepository.findByEmail(email)
            .map(UserDetailsImpl::build)
            .orElseThrow(() -> {
                log.error("사용자를 찾을 수 없음: {}", email);
                return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email);
            });
    }

    /**
     * 이메일로 사용자 DTO 조회
     */
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        log.debug("이메일로 사용자 DTO 조회: {}", email);

        return userRepository.findByEmail(email)
            .map(this::convertToDto)
            .orElseThrow(() -> {
                log.error("사용자를 찾을 수 없음: {}", email);
                return ResourceNotFoundException.userNotFound(email);
            });
    }

    /**
     * User 엔티티를 UserDTO로 변환
     */
    private UserDTO convertToDto(User user) {
        return new UserDTO(user.getId(), user.getEmail(), user.getRole());
    }
}