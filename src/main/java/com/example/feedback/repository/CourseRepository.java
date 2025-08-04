package com.example.feedback.repository;

import com.example.feedback.model.Course;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author Amna Hatem
 */
public interface CourseRepository extends MongoRepository<Course, String> {
    List<Course> findAllByInstructorId(String strings);
}
