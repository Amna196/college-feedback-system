package com.example.feedback.dto.response;

import com.example.feedback.model.Feedback;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Amna Hatem
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseFeedbackDTO {
    private String courseId;
    private String courseName;
    private double avgRating;
    private int feedbackCount;
    private List<Feedback> feedbacks;

}
