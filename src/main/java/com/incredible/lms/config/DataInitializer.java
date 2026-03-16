package com.incredible.lms.config;

import com.incredible.lms.entity.User;
import com.incredible.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        userRepository.findByEmail("adminincredible@gmail.com").ifPresentOrElse(
                user -> {
                    user.setName("Shakthi Sravanth");
                    user.setPassword(passwordEncoder.encode("adminshakthi@123"));
                    user.setRole(User.Role.ADMIN);
                    userRepository.save(user);
                },
                () -> {
                    User admin = User.builder()
                            .name("Shakthi Sravanth")
                            .email("adminincredible@gmail.com")
                            .password(passwordEncoder.encode("adminshakthi@123"))
                            .role(User.Role.ADMIN)
                            .build();
                    userRepository.save(admin);
                    System.out.println("Admin user initialized.");
                });

        userRepository.findByEmail("secondadmin@gmail.com").ifPresentOrElse(
                user -> {
                    user.setPassword(passwordEncoder.encode("admin@123"));
                    user.setRole(User.Role.ADMIN);
                    userRepository.save(user);
                },
                () -> {
                    User admin = User.builder()
                            .name("Second Admin")
                            .email("secondadmin@gmail.com")
                            .password(passwordEncoder.encode("admin@123"))
                            .role(User.Role.ADMIN)
                            .build();
                    userRepository.save(admin);
                    System.out.println("Second Admin user initialized.");
                });
    }
}
