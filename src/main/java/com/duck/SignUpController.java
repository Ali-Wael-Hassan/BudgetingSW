package com.duck;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignUpController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private TextField confirmPasswordTextField;

    @FXML
    private Label errorLabel;

    @FXML
    private Hyperlink signInLink;

    @FXML
    private void initialize() {
        passwordTextField.setManaged(passwordTextField.isVisible());
        confirmPasswordTextField.setManaged(confirmPasswordTextField.isVisible());
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        confirmPasswordTextField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
    }

    @FXML
    private void handleCreateAccount() {
        String password = passwordField.isVisible() ? passwordField.getText() : passwordTextField.getText();
        String confirmPassword = confirmPasswordField.isVisible() ? confirmPasswordField.getText()
                : confirmPasswordTextField.getText();

        if (password == null || password.length() < 8) {
            showError("Password must be at least 8 characters.");
            return;
        }
        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d).+$")) {
            showError("Password must contain both letters and numbers.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        clearError();
        System.out.println("Create Account clicked: " + fullNameField.getText());
        System.out.println("Calling showDashboard...");
        App.showDashboard();
    }

    @FXML
    private void handleGoogleContinue() {
        clearError();
        System.out.println("Continue with Google clicked");
    }

    @FXML
    private void handleSignIn() {
        clearError();
        System.out.println("Sign In clicked - navigating to Login screen");
        App.showLogin();
    }

    @FXML
    private void handleTogglePassword() {
        boolean showing = passwordTextField.isVisible();
        passwordTextField.setVisible(!showing);
        passwordTextField.setManaged(!showing);
        passwordField.setVisible(showing);
        passwordField.setManaged(showing);
    }

    @FXML
    private void handleToggleConfirmPassword() {
        boolean showing = confirmPasswordTextField.isVisible();
        confirmPasswordTextField.setVisible(!showing);
        confirmPasswordTextField.setManaged(!showing);
        confirmPasswordField.setVisible(showing);
        confirmPasswordField.setManaged(showing);
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
