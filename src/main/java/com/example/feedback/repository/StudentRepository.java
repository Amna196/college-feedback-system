package com.example.feedback.repository;

import com.example.feedback.model.Student;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Amna Hatem
 */
public interface StudentRepository extends MongoRepository<Student, String> {
    Student findByEmail(String email);
}
