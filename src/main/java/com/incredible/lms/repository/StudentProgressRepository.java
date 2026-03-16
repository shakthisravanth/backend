package com.incredible.lms.repository;

import com.incredible.lms.entity.StudentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentProgressRepository extends JpaRepository<StudentProgress, Long> {
    boolean existsByUserIdAndContentId(Long userId, Long contentId);
    int countByUserIdAndCourseId(Long userId, Long courseId);
    List<StudentProgress> findByUserIdAndCourseId(Long userId, Long courseId);
    void deleteByUserIdAndContentId(Long userId, Long contentId);
    void deleteByUserIdAndCourseId(Long userId, Long courseId);
}
