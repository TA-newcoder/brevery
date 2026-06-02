package com.brevery.config;

import com.brevery.entity.User;
import com.brevery.enums.Role;
import com.brevery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (userRepository.count() == 0) {
                log.info("Khởi tạo tài khoản test...");

                User admin = User.builder()
                        .email("admin@brevery.vn")
                        .passwordHash(passwordEncoder.encode("password"))
                        .fullName("Quản trị viên")
                        .phone("0901234567")
                        .role(Role.ADMIN)
                        .isActive(true)
                        .build();

                User khach = User.builder()
                        .email("khach@brevery.vn")
                        .passwordHash(passwordEncoder.encode("password"))
                        .fullName("Khách hàng Test")
                        .phone("0909876543")
                        .role(Role.USER)
                        .isActive(true)
                        .build();

                userRepository.save(admin);
                userRepository.save(khach);

                log.info("Đã tạo tài khoản test:");
                log.info("Admin: admin@brevery.vn / password");
                log.info("Khách: khach@brevery.vn / password");
            }
        };
    }
}
