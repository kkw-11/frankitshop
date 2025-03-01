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

    // 테스트용 초기 사용자 등록
    @PostConstruct
    public void init() {
        // 사용자가 없으면 테스트 사용자 생성
        if (!userRepository.existsByEmail("user@example.com")) {
            User user = new User();
            user.setEmail("user@example.com");
            user.setPassword(passwordEncoder.encode("password"));
            user.setRole("USER");
            userRepository.save(user);
        }

        if (!userRepository.existsByEmail("admin@example.com")) {
            User admin = new User();
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("password"));
            admin.setRole("ADMIN");
            userRepository.save(admin);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        return UserDetailsImpl.build(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        log.debug("이메일로 사용자 DTO 조회: {}", email);

        return userRepository.findByEmail(email)
            .map(user -> new UserDTO(user.getId(), user.getEmail(), user.getRole()))
            .orElseThrow(() -> {
                log.error("사용자를 찾을 수 없음: {}", email);
                return ResourceNotFoundException.userNotFound(email);
            });
    }
}
