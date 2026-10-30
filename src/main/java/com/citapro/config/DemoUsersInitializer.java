package com.citapro.config;

import com.citapro.users.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Configuration
public class DemoUsersInitializer {

    private static final List<String> DEMO_EMAILS = List.of(
        "admin@citapro.com",
        "carlos@citapro.com",
        "andrea@citapro.com",
        "cliente@citapro.com"
    );

    @Bean
    @Transactional
    public CommandLineRunner ensureDemoPasswords(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            for (String email : DEMO_EMAILS) {
                userRepository.findByEmailWithRoles(email).ifPresent(user -> {
                    if (!passwordEncoder.matches("Password123!", user.getPasswordHash())) {
                        user.setPasswordHash(passwordEncoder.encode("Password123!"));
                        userRepository.save(user);
                    }
                });
            }
        };
    }
}
