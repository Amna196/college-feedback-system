package com.example.feedback.service;

import com.example.feedback.dto.request.FilterType;
import com.example.feedback.dto.response.CourseFeedbackDTO;
import com.example.feedback.dto.response.FeedbackDetailDTO;
import com.example.feedback.dto.response.InstructorFeedbackDTO;
import com.example.feedback.model.Feedback;
import com.example.feedback.repository.CourseRepository;
import com.example.feedback.repository.FeedbackRepository;
import com.example.feedback.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Amna Hatem
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final FeedbackRepository feedbackRepo;
    private final CourseRepository courseRepo;
    private final InstructorRepository instructorRepo;

    public byte[] generateCSVBytes() {
        List<Feedback> feedbacks = feedbackRepo.findAll();
        StringWriter writer = new StringWriter();
        try (CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("StudentId", "CourseName", "Instructor", "Rating", "Comment", "CreatedAt"))) {
            for (Feedback f : feedbacks) {
                csvPrinter.printRecord(
                        f.getStudentId(),
                        f.getCourseName(),
                        f.getInstructorName(),
                        f.getRating(),
                        f.getComment(),
                        f.getCreatedAt()
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Error generating CSV", e);
        }
        return writer.toString().getBytes(StandardCharsets.UTF_8);
    }

    public ResponseEntity<?> getFeedbackSummaryList(FilterType type, String courseName, String instructorName) {
        return switch (type) {
            case COURSE -> ResponseEntity.ok(getCourseSummary(courseName));
            case INSTRUCTOR -> ResponseEntity.ok(getInstructorSummary(instructorName));
            default -> ResponseEntity.badRequest().body("Invalid type parameter");
        };
    }

    public List<CourseFeedbackDTO> getCourseSummary(String courseName) {
        return courseRepo.findAll().stream()
                .filter(c -> courseName == null || c.getName().toLowerCase().contains(courseName.toLowerCase()))
                .map(course -> {
                    var feedbacks = feedbackRepo.findAllByCourseId(course.getId());
                    var avgRating = feedbacks.stream().mapToDouble(Feedback::getRating).average().orElse(0);
                    return new CourseFeedbackDTO(course.getId(), course.getName(), avgRating, feedbacks.size(), feedbacks);
                })
                .toList();
    }

    public List<InstructorFeedbackDTO> getInstructorSummary(String instructorName) {
        return instructorRepo.findAll().stream()
                .filter(i -> instructorName == null || i.getFullName().toLowerCase().contains(instructorName.toLowerCase()))
                .map(instructor -> {
                    var courses = courseRepo.findAllByInstructorId(instructor.getId());
                    var allFeedbacks = courses.stream()
                            .flatMap(c -> feedbackRepo.findAllByCourseId(c.getId()).stream())
                            .toList();
                    var avgRating = allFeedbacks.stream().mapToDouble(Feedback::getRating).average().orElse(0);
                    return new InstructorFeedbackDTO(instructor.getId(), instructor.getFullName(), instructor.getDepartment(), avgRating, courses.size(), allFeedbacks.size());
                })
                .toList();
    }

    public List<FeedbackDetailDTO> getCourseFeedbackDetails(String courseName) {
        return courseRepo.findAll().stream()
                .filter(c -> courseName == null || c.getName().toLowerCase().contains(courseName.toLowerCase()))
                .flatMap(course -> feedbackRepo.findAllByCourseId(course.getId()).stream()
                        .map(f -> new FeedbackDetailDTO(
                                course.getName(),
                                f.getInstructorName(),
                                f.getRating(),
                                f.getComment(),
                                f.getCreatedAt()
                        ))
                )
                .toList();
    }
}

