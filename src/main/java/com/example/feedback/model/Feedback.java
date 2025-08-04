package com.example.feedback.model;

import com.vaadin.flow.component.template.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * @author Amna Hatem
 */
@Document(collection = "feedbacks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {
    @Id
    private String id;
    private String studentId;
    private String courseId;
    private String courseName;
    private String instructorId;
    private String instructorName;

    private double rating;
    private String comment;
    private LocalDateTime createdAt = LocalDateTime.now();
}
