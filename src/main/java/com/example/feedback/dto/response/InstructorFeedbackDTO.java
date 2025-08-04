package com.example.feedback.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Amna Hatem
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstructorFeedbackDTO {
    private String instructorId;
    private String instructorName;
    private String department;
    private double avgRating;
    private int totalCourses;
    private int feedbackCount;
}
