package com.example.feedback.repository;

import com.example.feedback.model.Enrollment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author Amna Hatem
 */
public interface EnrollmentRepository extends MongoRepository<Enrollment, String> {
    List<Enrollment> findByStudentId(String studentId);
    boolean existsByStudentIdAndCourseId(String studentId, String courseId);
}
