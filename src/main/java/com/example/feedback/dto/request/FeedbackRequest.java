package com.example.feedback.dto.request;

import lombok.Data;

/**
 * @author Amna Hatem
 */
@Data
public class FeedbackRequest {
    private double rating;
    private String comment;
}
