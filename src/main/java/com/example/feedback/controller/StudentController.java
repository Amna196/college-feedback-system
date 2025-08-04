package com.example.feedback.controller;

import com.example.feedback.dto.request.FeedbackRequest;
import com.example.feedback.model.Course;
import com.example.feedback.model.Enrollment;
import com.example.feedback.model.Student;
import com.example.feedback.repository.CourseRepository;
import com.example.feedback.repository.EnrollmentRepository;
import com.example.feedback.repository.StudentRepository;
import com.example.feedback.service.StudentService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Amna Hatem
 */
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final StudentRepository studentRepository;

    @GetMapping("/courses")
    public ResponseEntity<?> getEnrolledCourses(HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null || !"student".equals(claims.get("role"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        String email = claims.getSubject();
        Student student = studentRepository.findByEmail(email);
        if (student == null) return ResponseEntity.notFound().build();

        // Get all enrollments for this student
        var courses = studentService.getEnrolledCourses(student.getId());
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/courses/{courseId}/feedback")
    public ResponseEntity<?> submitFeedback(HttpServletRequest request, @PathVariable String courseId, @RequestBody FeedbackRequest feedbackRequest) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null || !"student".equals(claims.get("role"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        String studentId = (String) claims.get("id");

        try {
            return ResponseEntity.ok(studentService.submitFeedback(studentId, courseId, feedbackRequest.getRating(), feedbackRequest.getComment()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/courses/{courseId}/feedback")
    public ResponseEntity<?> viewFeedback(HttpServletRequest request, @PathVariable String courseId) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null || !"student".equals(claims.get("role"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        return ResponseEntity.ok(studentService.getFeedbackForCourse(courseId));
    }
}
