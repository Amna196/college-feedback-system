package com.example.feedback.repository;

import com.example.feedback.model.Feedback;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author Amna Hatem
 */

public interface FeedbackRepository extends MongoRepository<Feedback, String> {
    Optional<Feedback> findByStudentIdAndCourseId(String studentId, String courseId);
    List<Feedback> findAllByCourseId(String courseId);
}
