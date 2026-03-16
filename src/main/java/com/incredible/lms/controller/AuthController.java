package com.incredible.lms.controller;

import com.incredible.lms.dto.AuthenticationResponse;
import com.incredible.lms.repository.UserRepository;
import com.incredible.lms.security.JwtService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new RuntimeException("Invalid credentials");
        }

        com.incredible.lms.entity.User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        final String jwt = jwtService.generateToken(userDetails);

        String roleValue = user.getRole() != null ? user.getRole().name() : "NONE";

        // Emergency override for seeded admin account to ensure access
        if ("adminincredible@gmail.com".equalsIgnoreCase(user.getEmail())) {
            roleValue = "ADMIN";
        }



        return ResponseEntity.ok(AuthenticationResponse.builder()
                .token(jwt)
                .email(user.getEmail())
                .role(roleValue)
                .name(user.getName())
                .build());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthenticationRequest {
        private String email;
        private String password;
    }
}
