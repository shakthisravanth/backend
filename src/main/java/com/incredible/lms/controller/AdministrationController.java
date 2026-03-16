package com.incredible.lms.controller;

import com.incredible.lms.entity.User;
import com.incredible.lms.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/students")
@RequiredArgsConstructor
public class AdministrationController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegistrationResponse> registerUser(@RequestBody RegistrationRequest request) {
        String generatedPassword = generateRandomPassword();
        User.Role targetRole = request.getRole() != null ? request.getRole() : User.Role.STUDENT;

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(generatedPassword))
                .role(targetRole)
                .build();

        userRepository.save(user);

        String roleName = targetRole == User.Role.ADMIN ? "Administrator" : "Student";
        String copyReadyMessage = String.format(
                "Registration success! %s details: \nEmail: %s \nTemporary Password: %s\n\nThey can login at their respective portal and change their password anytime.",
                roleName,
                request.getEmail(),
                generatedPassword);

        return ResponseEntity.ok(RegistrationResponse.builder()
                .message(copyReadyMessage)
                .email(request.getEmail())
                .generatedPassword(generatedPassword)
                .build());
    }

    @PostMapping("/regenerate-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegistrationResponse> regeneratePassword(@RequestBody RegeneratePasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        String roleName = user.getRole() == User.Role.ADMIN ? "Administrator" : "Student";
        String copyReadyMessage = String.format(
                "Password regeneration success! %s details updated: \nEmail: %s \nNew Temporary Password: %s\n\nPlease share these new credentials with the user.",
                roleName,
                user.getEmail(),
                newPassword);

        return ResponseEntity.ok(RegistrationResponse.builder()
                .message(copyReadyMessage)
                .email(user.getEmail())
                .generatedPassword(newPassword)
                .build());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable("id") Long id, @RequestBody RegistrationRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        return ResponseEntity.ok(userRepository.save(user));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok().body("User deleted successfully.");
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<User>> getPaginatedStudents(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "search", required = false) String search) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        if (search != null && !search.trim().isEmpty()) {
            return ResponseEntity.ok(userRepository.findByRoleAndEmailContainingIgnoreCase(
                    User.Role.STUDENT, search, pageable));
        } else {
            return ResponseEntity.ok(userRepository.findByRole(User.Role.STUDENT, pageable));
        }
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        return random.ints(8, 0, chars.length())
                .mapToObj(chars::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegeneratePasswordRequest {
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistrationRequest {
        private String name;
        private String email;
        private String phone;
        private User.Role role;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistrationResponse {
        private String message;
        private String email;
        private String generatedPassword;
    }
}
