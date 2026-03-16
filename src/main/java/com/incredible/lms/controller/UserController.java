package com.incredible.lms.controller;

import com.incredible.lms.entity.User;
import com.incredible.lms.repository.UserRepository;
import com.incredible.lms.entity.Course;
import com.incredible.lms.entity.CourseContent;
import com.incredible.lms.entity.StudentProgress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.incredible.lms.repository.CourseRepository courseRepository;
    private final com.incredible.lms.repository.CourseContentRepository courseContentRepository;
    private final com.incredible.lms.repository.StudentProgressRepository studentProgressRepository;

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Incorrect current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Password changed successfully");
    }

    @GetMapping("/my-courses")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<?> getMyCourses() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        java.util.List<CourseProgressDTO> progressList = user.getCourses().stream().map(course -> {
            int totalContent = courseContentRepository.findByCourseIdOrderByOrderIndexAsc(course.getId()).size();
            int completedCount = studentProgressRepository.countByUserIdAndCourseId(user.getId(), course.getId());
            int progressPercentage = totalContent == 0 ? 0 : (int) Math.round(((double) completedCount / totalContent) * 100);
            
            return CourseProgressDTO.builder()
                    .id(course.getId())
                    .name(course.getName())
                    .description(course.getDescription())
                    .progress(progressPercentage)
                    .build();
        }).toList();
        
        return ResponseEntity.ok(progressList);
    }

    @PostMapping("/courses/{courseId}/content/{contentId}/complete")
    @PreAuthorize("hasRole('STUDENT')")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> markContentAsComplete(
            @PathVariable("courseId") Long courseId,
            @PathVariable("contentId") Long contentId) {
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        if (studentProgressRepository.existsByUserIdAndContentId(user.getId(), contentId)) {
            return ResponseEntity.ok(java.util.Map.of("message", "Already completed", "completed", true));
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
                
        CourseContent content = courseContentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        if (!content.getCourse().getId().equals(courseId)) {
            return ResponseEntity.badRequest().body("Content does not belong to this course");
        }

        StudentProgress progress = StudentProgress.builder()
                .user(user)
                .course(course)
                .content(content)
                .build();
                
        studentProgressRepository.save(progress);
        
        return ResponseEntity.ok(java.util.Map.of("message", "Marked as complete", "completed", true));
    }

    @DeleteMapping("/courses/{courseId}/content/{contentId}/undo")
    @PreAuthorize("hasRole('STUDENT')")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> undoContentCompletion(
            @PathVariable("courseId") Long courseId,
            @PathVariable("contentId") Long contentId) {
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        studentProgressRepository.deleteByUserIdAndContentId(user.getId(), contentId);
        
        return ResponseEntity.ok(java.util.Map.of("message", "Progress undone", "completed", false));
    }

    @PostMapping("/courses/{courseId}/restart")
    @PreAuthorize("hasRole('STUDENT')")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> restartCourse(@PathVariable("courseId") Long courseId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        studentProgressRepository.deleteByUserIdAndCourseId(user.getId(), courseId);
        
        return ResponseEntity.ok(java.util.Map.of("message", "Course restarted successfully"));
    }

    @GetMapping("/courses/{courseId}/progress")
    @PreAuthorize("hasRole('STUDENT')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<?> getCourseProgress(@PathVariable("courseId") Long courseId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        java.util.List<StudentProgress> completed = studentProgressRepository.findByUserIdAndCourseId(user.getId(), courseId);
        java.util.List<Long> completedContentIds = completed.stream().map(p -> p.getContent().getId()).toList();
        
        int totalContent = courseContentRepository.findByCourseIdOrderByOrderIndexAsc(courseId).size();
        int completedCount = completedContentIds.size();
        
        int progressPercentage = totalContent == 0 ? 0 : (int) Math.round(((double) completedCount / totalContent) * 100);
        
        return ResponseEntity.ok(java.util.Map.of(
            "completedContentIds", completedContentIds,
            "progress", progressPercentage,
            "completedCount", completedCount,
            "totalCount", totalContent
        ));
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseProgressDTO {
        private Long id;
        private String name;
        private String description;
        private int progress;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;
    }
}
