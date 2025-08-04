package com.example.feedback.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.Map;

import com.vaadin.flow.server.VaadinSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


/**
 * @author Amna Hatem
 */
@Route("login")
@PageTitle("Login")
public class LoginView extends VerticalLayout {

    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H1 title = new H1("Student Feedback Portal - College Login");
        TextField email = new TextField("Email");
        PasswordField password = new PasswordField("Password");
        Button loginBtn = new Button("Login");

        Div form = new Div(email, password, loginBtn);
        form.getStyle().set("display", "flex").set("flexDirection", "column").set("gap", "10px");

        loginBtn.addClickListener(e -> {
            try {
                Map<String, String> payload = Map.of(
                        "email", email.getValue(),
                        "password", password.getValue()
                );

                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<Map> res = restTemplate.postForEntity("http://localhost:8082/api/auth/login", payload, Map.class);

                Map<String, Object> body = res.getBody();
                VaadinSession.getCurrent().setAttribute("token", body.get("token"));
                VaadinSession.getCurrent().setAttribute("id", body.get("id"));
                VaadinSession.getCurrent().setAttribute("role", body.get("role"));
                VaadinSession.getCurrent().setAttribute("email", body.get("email"));
                VaadinSession.getCurrent().setAttribute("fullName", body.get("fullName"));

                String role = (String) body.get("role");
                if ("admin".equals(role)) {
                    UI.getCurrent().navigate("admin/dashboard");
                } else {
                    UI.getCurrent().navigate("student/dashboard");
                }

            } catch (HttpClientErrorException ex) {
                Notification.show("Invalid credentials", 3000, Notification.Position.MIDDLE);
            }
        });

        add(title, form);
    }
}
