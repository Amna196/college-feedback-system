package com.example.feedback.config;

import com.example.feedback.model.Student;
import com.example.feedback.model.User;
import com.example.feedback.repository.StudentRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * @author Amna Hatem
 */
@Component
public class JwtUtil {
    @Autowired
    private StudentRepository studentRepository;

    private final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateToken(User user) {
        String userId = null;
        if (user.getRole().equalsIgnoreCase("student")) {
            Student student = studentRepository.findByEmail(user.getEmail());
            userId = student.getId();
        } else {
            userId = user.getId();
        }
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole())
                .claim("id", userId)
                .claim("fullName", user.getFullName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                .signWith(SECRET_KEY)
                .compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }
}
