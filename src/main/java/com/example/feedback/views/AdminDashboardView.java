package com.example.feedback.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

/**
 * @author Amna Hatem
 */
@Route("admin/dashboard")
@PageTitle("Admin Dashboard")
public class AdminDashboardView extends VerticalLayout {

    private final Grid<Map<String, Object>> feedbackGrid = new Grid<>();
    private final ComboBox<String> filterType = new ComboBox<>("Filter By", "COURSE", "INSTRUCTOR");
    private final TextField filterValue = new TextField();
    private final Button loadFeedbackBtn = new Button("Load Feedback");
    private final Button exportCsvBtn = new Button("Export CSV");
    private final Dialog feedbackDialog = new Dialog();

    public AdminDashboardView() {
        String role = (String) VaadinSession.getCurrent().getAttribute("role");
        if (!"admin".equals(role)) {
            UI.getCurrent().navigate("login");
            return;
        }

        setSizeFull();
        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Feedback Summary");

        filterType.setValue("INSTRUCTOR"); // default
        filterValue.setPlaceholder("Enter Instructor Name");

        filterType.addValueChangeListener(e -> {
            if ("COURSE".equals(e.getValue())) {
                filterValue.setLabel("Course Name");
                filterValue.setPlaceholder("Enter Course Name");
            } else {
                filterValue.setLabel("Instructor Name");
                filterValue.setPlaceholder("Enter Instructor Name");
            }
        });

        HorizontalLayout controls = new HorizontalLayout(filterType, filterValue, loadFeedbackBtn, exportCsvBtn);
        controls.setAlignItems(Alignment.BASELINE);

        loadFeedbackBtn.addClickListener(e -> fetchFeedback());
        exportCsvBtn.addClickListener(e -> downloadCsv());

        add(title, controls, feedbackGrid);
        fetchFeedback(); // auto-load default (INSTRUCTOR)
    }

    private void fetchFeedback() {
        String token = (String) VaadinSession.getCurrent().getAttribute("token");
        if (token == null) {
            Notification.show("Login required");
            UI.getCurrent().navigate("login");
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String type = filterType.getValue();
        String filterKey = type.equals("COURSE") ? "courseName" : "instructorName";

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl("http://localhost:8082/api/admin/feedback-summary")
                .queryParam("type", type);

        if (!filterValue.getValue().isBlank()) {
            builder.queryParam(filterKey, filterValue.getValue());
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<List> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    List.class
            );

            List<Map<String, Object>> data = response.getBody();
            updateGrid(type, data);
        } catch (Exception e) {
            Notification.show("Failed to fetch feedback");
        }
    }

    private void updateGrid(String type, List<Map<String, Object>> data) {
        feedbackGrid.removeAllColumns();

        if ("INSTRUCTOR".equals(type)) {
            feedbackGrid.addColumn(map -> map.get("instructorName")).setHeader("Instructor");
            feedbackGrid.addColumn(map -> map.get("department")).setHeader("Department");
            feedbackGrid.addColumn(map -> map.get("avgRating")).setHeader("Avg Rating");
            feedbackGrid.addColumn(map -> map.get("totalCourses")).setHeader("Courses");
            feedbackGrid.addComponentColumn(map -> {
                Integer count = ((Number) map.get("feedbackCount")).intValue();
                Icon icon = new Icon(VaadinIcon.COMMENTS);
                icon.setColor("blue");
                icon.setSize("20px");
                icon.getElement().setProperty("title", count + " feedbacks");
                return icon;
            }).setHeader("Feedbacks");

        } else {
            feedbackGrid.addColumn(map -> map.get("courseName")).setHeader("Course");
            feedbackGrid.addColumn(map -> map.get("avgRating")).setHeader("Avg Rating");

            feedbackGrid.addComponentColumn(map -> {
                Integer count = ((Number) map.get("feedbackCount")).intValue();
                Button feedbackBtn = new Button(count + " Feedbacks", VaadinIcon.COMMENTS.create());
                feedbackBtn.addClickListener(e -> openFeedbackDialog((List<Map<String, Object>>) map.get("feedbacks")));
                return feedbackBtn;
            }).setHeader("Feedbacks");
        }

        feedbackGrid.setItems(data);
    }

    private void openFeedbackDialog(List<Map<String, Object>> feedbacks) {
        VerticalLayout content = new VerticalLayout();
        content.setWidth("400px");

        if (feedbacks == null || feedbacks.isEmpty()) {
            content.add(new Span("No feedback available."));
        } else {
            for (Map<String, Object> fb : feedbacks) {
                Span instructorName = new Span("Instructor: " + fb.get("instructorName"));
                Span rating = new Span("Rating: " + fb.get("rating"));
                Span comment = new Span("Comment: " + fb.get("comment"));
                comment.getStyle().set("font-style", "italic");
                comment.getStyle().set("color", "#555");
                content.add(new Hr(),instructorName, rating, comment);
            }
        }

        feedbackDialog.removeAll();
        feedbackDialog.add(content);
        feedbackDialog.setWidth("420px");
        feedbackDialog.setHeight("auto");
        feedbackDialog.setModal(true);
        feedbackDialog.open();
    }

    private void downloadCsv() {
        String token = (String) VaadinSession.getCurrent().getAttribute("token");

        if (token == null) {
            Notification.show("You must be logged in to download the file.");
            return;
        }

        try {
            // Set Authorization header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            // Make the HTTP request
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    "http://localhost:8082/api/admin/feedback/export",
                    HttpMethod.GET,
                    requestEntity,
                    byte[].class
            );

            byte[] csvBytes = response.getBody();

            // Create StreamResource to allow download in browser
            StreamResource resource = new StreamResource("feedback.csv", () -> new ByteArrayInputStream(csvBytes));
            resource.setContentType("text/csv");

            Anchor downloadLink = new Anchor(resource, "Download Feedback CSV");
            downloadLink.getElement().setAttribute("download", true);

            // Show the link in the UI (or trigger click)
            add(downloadLink);
            downloadLink.getElement().callJsFunction("click"); // optional auto-click

        } catch (Exception e) {
            Notification.show("Failed to download CSV: " + e.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

//    private void downloadCsv() {
//        String token = (String) VaadinSession.getCurrent().getAttribute("token");
//
//        if (token == null) {
//            Notification.show("You must be logged in to download the file.");
//            return;
//        }
//
//        try {
//            HttpHeaders headers = new HttpHeaders();
//            headers.setBearerAuth(token);
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
//
//            RestTemplate restTemplate = new RestTemplate();
//            ResponseEntity<byte[]> response = restTemplate.exchange(
//                    "http://localhost:8082/api/admin/feedback/export",
//                    HttpMethod.GET,
//                    requestEntity,
//                    byte[].class
//            );
//
//            byte[] csvBytes = response.getBody();
//
//            StreamResource resource = new StreamResource("feedback.csv", () -> new ByteArrayInputStream(csvBytes));
//            resource.setContentType("text/csv");
//
//            Anchor downloadLink = new Anchor(resource, "Download Feedback CSV");
//            downloadLink.getElement().setAttribute("download", true);
//            downloadLink.setVisible(false); // hide from layout
//
//            add(downloadLink);
//            downloadLink.getElement().callJsFunction("click");
//
//        } catch (Exception e) {
//            Notification.show("Failed to download CSV: " + e.getMessage(), 4000, Notification.Position.MIDDLE);
//        }
//    }
}

