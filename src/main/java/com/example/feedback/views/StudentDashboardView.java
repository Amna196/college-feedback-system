package com.example.feedback.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * @author Amna Hatem
 */
@Route("student/dashboard")
@PageTitle("Student Dashboard")
public class StudentDashboardView extends VerticalLayout {

    private final Grid<Map<String, Object>> courseGrid = new Grid<>();
    private final Dialog feedbackDialog = new Dialog();

    public StudentDashboardView() {
        String role = (String) VaadinSession.getCurrent().getAttribute("role");
        if (!"student".equals(role)) {
            UI.getCurrent().navigate("login");
            return;
        }

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 header = new H2("Enrolled Courses");

        courseGrid.addColumn(map -> map.get("name")).setHeader("Course Name");
        courseGrid.addComponentColumn(this::createFeedbackActionColumn).setHeader("Action");

        add(header, courseGrid);
        fetchCourses();
    }

    private void fetchCourses() {
        String token = (String) VaadinSession.getCurrent().getAttribute("token");
        if (token == null) {
            UI.getCurrent().navigate("login");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<List> response = restTemplate.exchange(
                    "http://localhost:8082/api/students/courses",
                    HttpMethod.GET,
                    entity,
                    List.class
            );

            List<Map<String, Object>> courses = response.getBody();
            courseGrid.setItems(courses);

        } catch (Exception e) {
            Notification.show("Failed to load courses");
        }
    }

    private Component createFeedbackActionColumn(Map<String, Object> course) {
        String courseId = (String) course.get("id");
        String studentId = (String) VaadinSession.getCurrent().getAttribute("id");
        String token = (String) VaadinSession.getCurrent().getAttribute("token");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<List> response = restTemplate.exchange(
                    "http://localhost:8082/api/students/courses/" + courseId + "/feedback",
                    HttpMethod.GET,
                    entity,
                    List.class
            );

            List<Map<String, Object>> feedbacks = response.getBody();

            boolean studentHasSubmitted = feedbacks != null && feedbacks.stream()
                    .anyMatch(fb -> studentId.equals(fb.get("studentId")));

            if (studentHasSubmitted) {
                Button viewBtn = new Button("View Feedbacks", VaadinIcon.EYE.create());
                viewBtn.addClickListener(e -> openFeedbackDialog(feedbacks));
                return viewBtn;
            } else {
                Button submitBtn = new Button("Submit Feedback", VaadinIcon.EDIT.create());
                submitBtn.addClickListener(e -> openSubmitDialog(courseId));
                return submitBtn;
            }

        } catch (Exception e) {
            return new Span("Error");
        }
    }

    private void openFeedbackDialog(List<Map<String, Object>> feedbacks) {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidth("400px");

        if (feedbacks == null || feedbacks.isEmpty()) {
            layout.add(new Span("No feedback available."));
        } else {
            for (Map<String, Object> fb : feedbacks) {
                Div block = new Div();
                block.add(new Span("Rating: " + fb.get("rating")));
                block.add(new Span("Comment: " + fb.get("comment")));
                block.getStyle().set("margin-bottom", "10px");
                layout.add(new Hr(), block);
            }
        }

        feedbackDialog.removeAll();
        feedbackDialog.add(layout);
        feedbackDialog.setModal(true);
        feedbackDialog.open();
    }

    private void openSubmitDialog(String courseId) {
        NumberField rating = new NumberField("Rating (0-5)");
        rating.setMin(0.0);
        rating.setMax(5.0);
        rating.setStep(0.5);

        TextArea comment = new TextArea("Comment");
        comment.setWidthFull();

        Button submit = new Button("Submit Feedback", e -> submitFeedback(courseId, rating.getValue(), comment.getValue()));

        VerticalLayout form = new VerticalLayout(rating, comment, submit);
        form.setWidth("400px");

        feedbackDialog.removeAll();
        feedbackDialog.add(form);
        feedbackDialog.open();
    }

    private void submitFeedback(String courseId, Double rating, String comment) {
        String token = (String) VaadinSession.getCurrent().getAttribute("token");

        if (rating == null || comment == null || comment.trim().isEmpty()) {
            Notification.show("All fields required");
            return;
        }

        Map<String, Object> payload = Map.of(
                "rating", rating,
                "comment", comment
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForEntity("http://localhost:8082/api/students/courses/" + courseId + "/feedback", request, Void.class);

            Notification.show("Feedback submitted!");
            feedbackDialog.close();
            fetchCourses();

        } catch (HttpClientErrorException.BadRequest e) {
            Notification.show("Feedback already submitted", 3000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            Notification.show("Failed to submit feedback");
        }
    }
}


