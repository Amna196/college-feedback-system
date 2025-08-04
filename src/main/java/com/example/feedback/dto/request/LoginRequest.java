package com.example.feedback.dto.request;

import lombok.Data;

/**
 * @author Amna Hatem
 */
@Data
public class LoginRequest {
    private String email;
    private String password;
}
