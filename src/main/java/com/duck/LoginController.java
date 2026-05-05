package com.duck;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private Label errorLabel;

    @FXML
    private Hyperlink signUpLink;

    @FXML
    private void initialize() {
        passwordTextField.setManaged(passwordTextField.isVisible());
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    private void handleSignIn() {
        String email = emailField.getText();
        String password = passwordField.isVisible() ? passwordField.getText() : passwordTextField.getText();

        if (email == null || email.isEmpty()) {
            showError("Email is required.");
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Please enter a valid email address.");
            return;
        }
        if (password == null || password.isEmpty()) {
            showError("Password is required.");
            return;
        }

        clearError();
        System.out.println("Sign In clicked: " + emailField.getText());
        System.out.println("Calling showDashboard...");
        App.showDashboard();
    }

    @FXML
    private void handleGoogleContinue() {
        clearError();
        System.out.println("Continue with Google clicked");
    }

    @FXML
    private void handleForgotPassword() {
        clearError();
        System.out.println("Forgot password clicked");
        // TODO: navigate to password reset flow
    }

    @FXML
    private void handleSignUp() {
        clearError();
        System.out.println("Sign Up clicked - navigating to Sign Up screen");
        App.showSignUp();
    }

    @FXML
    private void handleTogglePassword() {
        boolean showing = passwordTextField.isVisible();
        passwordTextField.setVisible(!showing);
        passwordTextField.setManaged(!showing);
        passwordField.setVisible(showing);
        passwordField.setManaged(showing);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
