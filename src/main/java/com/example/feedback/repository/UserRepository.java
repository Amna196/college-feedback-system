package com.example.feedback.repository;

import com.example.feedback.model.Student;
import com.example.feedback.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Amna Hatem
 */
public interface UserRepository extends MongoRepository<User, String> {
    User findByEmail(String email);
}
