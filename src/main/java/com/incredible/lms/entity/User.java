package com.incredible.lms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private java.lang.String name;

    @Column(unique = true, nullable = false)
    private java.lang.String email;

    @Column(nullable = false)
    private java.lang.String password;

    @Column
    private java.lang.String phone;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(updatable = false)
    private java.time.LocalDateTime createdAt;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "user_courses",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id"))
    private java.util.Set<Course> courses = new java.util.HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }

    public enum Role {
        ADMIN, STUDENT
    }
}
