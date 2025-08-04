package com.example.feedback.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Amna Hatem
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackDetailDTO {
    private String courseName;
    private String instructorName;
    private double rating;
    private String comment;
    private LocalDateTime createdAt;
}

