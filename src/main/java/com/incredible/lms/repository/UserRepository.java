package com.incredible.lms.repository;

import com.incredible.lms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Page<User> findByRole(User.Role role, Pageable pageable);
    Page<User> findByRoleAndEmailContainingIgnoreCase(User.Role role, String email, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT u.id FROM User u JOIN u.courses c WHERE c.id = :courseId")
    java.util.List<Long> findStudentIdsByCourseId(@org.springframework.data.repository.query.Param("courseId") Long courseId);
}
