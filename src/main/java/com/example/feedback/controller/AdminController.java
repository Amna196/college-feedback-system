package com.example.feedback.controller;

import com.example.feedback.dto.request.FilterType;
import com.example.feedback.service.AdminService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Amna Hatem
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/feedback-summary")
    public ResponseEntity<?> getFeedbackSummary(HttpServletRequest request, @RequestParam FilterType type,
                                                @RequestParam(required = false) String courseName,
                                                @RequestParam(required = false) String instructorName) {

        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null || !"admin".equals(claims.get("role"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        return adminService.getFeedbackSummaryList(type, courseName, instructorName);
    }

    @GetMapping("/feedback/export")
    public ResponseEntity<?> exportCSV(HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null || !"admin".equals(claims.get("role"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        ByteArrayResource resource = new ByteArrayResource(adminService.generateCSVBytes());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=feedback.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(resource.contentLength())
                .body(resource);
    }
}
