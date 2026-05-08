package com.duck;

import com.duck.model.authentication.AppAuth;
import com.duck.model.authentication.Recognition;
import com.duck.model.authentication.SignUp;
import com.duck.model.type.Account;
import com.duck.model.type.AppSettings.Message;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignUpController {

    private Recognition authEngine = new SignUp();

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField passwordTextField;
    @FXML private TextField confirmPasswordTextField;
    @FXML private Label errorLabel;

    public SignUpController() {
        authEngine.setAuthStrategy(new AppAuth());
    }

    @FXML
    private void initialize() {
        passwordTextField.setManaged(passwordTextField.isVisible());
        confirmPasswordTextField.setManaged(confirmPasswordTextField.isVisible());
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        confirmPasswordTextField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
    }

    @FXML
    private void handleCreateAccount() {
        String username = fullNameField.getText();
        String rawEmail = emailField.getText();
        String password = passwordField.isVisible() ? passwordField.getText() : passwordTextField.getText();
        String confirmPassword = confirmPasswordField.isVisible() ? confirmPasswordField.getText() : confirmPasswordTextField.getText();

        if (rawEmail == null || rawEmail.trim().isEmpty() || !rawEmail.contains("@")) {
            showError("Email must be in format: address@example.com");
            return;
        }
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

        Account acc = new Account(rawEmail, username, password, 0.0f, null);
        
        Message check = authEngine.perform(acc);

        if (check == Message.SUCCESS) {
            ApplicationState.getInstance().initializeSession(rawEmail.trim());
            App.showDashboard();
        } else {
            displayError(check);
        }
    }

    private void displayError(Message flag) {
        switch (flag) {
            case INVALID_EMAIL:
                showError("Email is improperly formatted.");
                break;
            case ERROR:
                showError("Failed to create account. Please try again.");
                break;
            default:
                showError("Account creation failed. Email may already exist.");
                break;
        }
    }

    @FXML
    private void handleSignIn() {
        clearError();
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