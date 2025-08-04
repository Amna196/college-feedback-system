package com.example.feedback.model;

import com.vaadin.flow.component.template.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Amna Hatem
 */
@Document(collection = "instructors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Instructor {
    @Id
    private String id;
    private String fullName;
    private String department;
}

