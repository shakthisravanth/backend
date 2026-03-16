package com.incredible.lms.controller;

import com.incredible.lms.entity.Course;
import com.incredible.lms.entity.CourseContent;
import com.incredible.lms.entity.User;
import com.incredible.lms.repository.CourseContentRepository;
import com.incredible.lms.repository.CourseRepository;
import com.incredible.lms.repository.UserRepository;
import com.incredible.lms.repository.StudentProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseContentRepository courseContentRepository;
    private final StudentProgressRepository studentProgressRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        return ResponseEntity.ok(courseRepository.save(course));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseRepository.findAll());
    }

    @PostMapping("/{courseId}/assign/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> assignStudentToCourse(@PathVariable("courseId") Long courseId, @PathVariable("studentId") Long studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getRole() != User.Role.STUDENT) {
            return ResponseEntity.badRequest().body("Only students can be assigned to courses");
        }

        student.getCourses().add(course);
        userRepository.save(student);

        return ResponseEntity.ok("Student assigned to course successfully");
    }

    @DeleteMapping("/{courseId}/remove/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> removeStudentFromCourse(@PathVariable("courseId") Long courseId, @PathVariable("studentId") Long studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (!student.getCourses().contains(course)) {
            return ResponseEntity.badRequest().body("Student is not assigned to this course");
        }

        student.getCourses().remove(course);
        userRepository.save(student);
        
        // Remove their progress data for this course since they are unassigned
        studentProgressRepository.deleteByUserIdAndCourseId(studentId, courseId);

        return ResponseEntity.ok("Student removed from course successfully");
    }

    @GetMapping("/{courseId}/students")
    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<Long>> getEnrolledStudentIds(@PathVariable("courseId") Long courseId) {
        return ResponseEntity.ok(userRepository.findStudentIdsByCourseId(courseId));
    }

    @PostMapping("/{courseId}/content")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseContent> addCourseContent(@PathVariable("courseId") Long courseId, @RequestBody CourseContent content) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        content.setCourse(course);
        return ResponseEntity.ok(courseContentRepository.save(content));
    }

    @GetMapping("/{courseId}/content")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<List<CourseContent>> getCourseContent(@PathVariable("courseId") Long courseId) {
        return ResponseEntity.ok(courseContentRepository.findByCourseIdOrderByOrderIndexAsc(courseId));
    }

    @PutMapping("/{courseId}/content/{contentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseContent> updateCourseContent(@PathVariable("courseId") Long courseId, @PathVariable("contentId") Long contentId, @RequestBody CourseContent contentDetails) {
        CourseContent content = courseContentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Course content not found"));
        
        if (!content.getCourse().getId().equals(courseId)) {
            return ResponseEntity.badRequest().build();
        }

        content.setTitle(contentDetails.getTitle());
        content.setTextContent(contentDetails.getTextContent());
        content.setVideoUrl(contentDetails.getVideoUrl());
        content.setNotesUrl(contentDetails.getNotesUrl());
        content.setOrderIndex(contentDetails.getOrderIndex());

        return ResponseEntity.ok(courseContentRepository.save(content));
    }

    @DeleteMapping("/{courseId}/content/{contentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCourseContent(@PathVariable("courseId") Long courseId, @PathVariable("contentId") Long contentId) {
        CourseContent content = courseContentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Course content not found"));

        if (!content.getCourse().getId().equals(courseId)) {
            return ResponseEntity.badRequest().build();
        }

        courseContentRepository.delete(content);
        return ResponseEntity.ok("Course content deleted successfully");
    }
}
