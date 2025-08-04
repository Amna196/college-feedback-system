package com.example.feedback.views;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.BrowserType.LaunchOptions;

import java.lang.management.ManagementFactory;

public class LoginViewIT {

    Page page;

    @BeforeEach
    public void setup() {
        String args = ManagementFactory.getRuntimeMXBean().getInputArguments().toString();
        Boolean headed = args.contains("jdwp") || Boolean.getBoolean("headed");
        LaunchOptions ops = new BrowserType.LaunchOptions().setHeadless(!headed);
        page = Playwright.create().chromium().launch(ops).newContext().newPage();
        page.setDefaultTimeout(30000);
        page.navigate("http://localhost:8082/login");
    }

    @AfterEach
    public void tearDown() {
        page.context().browser().close();
    }

    @Test
    public void testInitialStateOfLoginView() throws Exception {
        // Given the user is on the page LoginView

        // Then the user should see a vertical layout with tag name 'vaadin-vertical-layout'
        Locator element = page.locator("vaadin-vertical-layout");
        PlaywrightAssertions.assertThat(element).isVisible();

        // And the user should see a text field with role 'textbox' and label 'Email'
        Locator textField = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Email"));
        PlaywrightAssertions.assertThat(textField).isVisible();

        // And the user should see a password field with role 'textbox' and label 'Password'
        Locator passwordField = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Password"));
        PlaywrightAssertions.assertThat(passwordField).isVisible();

        // And the user should see a button with role 'button' and label 'Login'
        Locator button = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login"));
        PlaywrightAssertions.assertThat(button).isVisible();

        // And the user should see a label
        Locator label = page.locator("vaadin-vertical-layout label");
        PlaywrightAssertions.assertThat(label).isVisible();
    }

    @Test
    public void testUserLeavesEmailAndPasswordFieldsEmptyAndClicksOnTheLoginButton() throws Exception {
        // Given the user is on the page LoginView

        // When the user clicks on the button with role 'button' and label 'Login'
        Locator button = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login"));
        button.click();

        // Then the label should appear with the text 'Please fill in both fields.'
        Locator label = page.locator("vaadin-vertical-layout label");
        PlaywrightAssertions.assertThat(label).hasText("Please fill in both fields.");
    }

    @Test
    public void testUserClicksOnTheLoginButtonWithFilledCredentialsButInvalid() throws Exception {
        // Given the user is on the page LoginView

        // And the user has entered 'invalid@example.com' in the text field with role 'textbox' and label 'Email'
        Locator emailField = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Email"));
        emailField.fill("invalid@example.com");

        // And the user has entered 'incorrectpassword' in the password field with role 'textbox' and label 'Password'
        Locator passwordField = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Password"));
        passwordField.fill("incorrectpassword");

        // When the user clicks on the button with role 'button' and label 'Login'
        Locator button = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login"));
        button.click();

        // Then the label should appear with the text 'Invalid credentials'
        Locator label = page.locator("vaadin-vertical-layout label");
        PlaywrightAssertions.assertThat(label).hasText("Invalid credentials");
    }

    @Test
    public void testUserClicksOnTheLoginButtonWithSuccessfulLogin() throws Exception {
        // Given the user is on the page LoginView

        // And the user has entered 'valid@example.com' in the text field with role 'textbox' and label 'Email'
        Locator emailField = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Email"));
        emailField.fill("valid@example.com");

        // And the user has entered 'correctpassword' in the password field with role 'textbox' and label 'Password'
        Locator passwordField = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Password"));
        passwordField.fill("correctpassword");

        // When the user clicks on the button with role 'button' and label 'Login'
        Locator button = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login"));
        button.click();

        // Then a notification should appear with the text 'Login successful!'
        Locator notification = page.locator("vaadin-notification");
        PlaywrightAssertions.assertThat(notification).hasText("Login successful!");
    }
}