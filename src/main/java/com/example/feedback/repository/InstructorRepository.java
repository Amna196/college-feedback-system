package com.example.feedback.repository;

import com.example.feedback.model.Instructor;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Amna Hatem
 */
public interface InstructorRepository extends MongoRepository<Instructor, String> {
}
