package com.duck;

import com.duck.model.authentication.AppAuth;
import com.duck.model.authentication.Recognition;
import com.duck.model.authentication.Login;
import com.duck.model.type.Account;
import com.duck.model.type.AppSettings.Message;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    private Recognition authEngine = new Login();

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private Label errorLabel;
    @FXML private Hyperlink signUpLink;

    public LoginController() {
        authEngine.setAuthStrategy(new AppAuth());
    }

    @FXML
    private void initialize() {
        if (passwordTextField != null && passwordField != null) {
            passwordTextField.setManaged(passwordTextField.isVisible());
            passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        }
    }

    @FXML
    private void handleSignIn() {
        String rawEmail = emailField.getText();
        String password = passwordField.isVisible() ? passwordField.getText() : passwordTextField.getText();

        if (rawEmail == null || rawEmail.trim().isEmpty() || !rawEmail.contains("@")) {
            showError("Email must be in format: address@example.com");
            return;
        }
        if (password == null || password.isEmpty()) {
            showError("Password cannot be empty.");
            return;
        }

        clearError();

        Account acc = new Account(rawEmail, null, password, 0.0f, null);
        
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
            case ERROR:
                showError("Invalid email or password.");
                break;
            default:
                showError("Login failed. Please verify credentials.");
                break;
        }
    }

    @FXML
    private void handleSignUp() {
        clearError();
        App.showSignUp();
    }

    @FXML
    private void handleTogglePassword() {
        if (passwordTextField != null && passwordField != null) {
            boolean showing = passwordTextField.isVisible();
            passwordTextField.setVisible(!showing);
            passwordTextField.setManaged(!showing);
            passwordField.setVisible(showing);
            passwordField.setManaged(showing);
        }
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