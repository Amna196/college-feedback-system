package com.example.feedback.service;

import com.example.feedback.model.Course;
import com.example.feedback.model.Enrollment;
import com.example.feedback.model.Feedback;
import com.example.feedback.model.Instructor;
import com.example.feedback.model.Student;
import com.example.feedback.repository.CourseRepository;
import com.example.feedback.repository.EnrollmentRepository;
import com.example.feedback.repository.FeedbackRepository;
import com.example.feedback.repository.InstructorRepository;
import com.example.feedback.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author Amna Hatem
 */
@Service
@RequiredArgsConstructor
public class StudentService {

    private final FeedbackRepository feedbackRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;

    public Feedback submitFeedback(String studentId, String courseId, double rating, String comment) {
        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new IllegalArgumentException("Student not enrolled in course");
        }
        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }

        Optional<Feedback> existing = feedbackRepository.findByStudentIdAndCourseId(studentId, courseId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Feedback already submitted");
        }

        Student student = studentRepository.findById(studentId).orElseThrow();
        Course course = courseRepository.findById(courseId).orElseThrow();
        Instructor instructor = instructorRepository.findById(course.getInstructorId()).orElseThrow();

        Feedback feedback = new Feedback();
        feedback.setStudentId(student.getId());
        feedback.setCourseId(course.getId());
        feedback.setCourseName(course.getName());
        feedback.setInstructorId(course.getInstructorId());
        feedback.setInstructorName(instructor.getFullName());
        feedback.setRating(rating);
        feedback.setComment(comment);
        return feedbackRepository.save(feedback);
    }

    public List<Feedback> getFeedbackForCourse(String courseId) {
        return feedbackRepository.findAllByCourseId(courseId);
    }

    public List<Course> getEnrolledCourses(String id) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(id);

        // Extract courseIds from enrollments
        List<String> courseIds = enrollments.stream()
                .map(Enrollment::getCourseId)
                .toList();

        // Get all corresponding courses
        return courseRepository.findAllById(courseIds);
    }
}
