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

/**
 * FXML controller for the user registration screen.  Validates inputs,
 * creates a new Account, and delegates authentication to the SignUp
 * strategy.  Also manages show/hide toggles for password fields.
 */
public class SignUpController {

    private Recognition authEngine = new SignUp();

    // =========================================================================
    // FXML Controls
    // =========================================================================

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField passwordTextField;
    @FXML private TextField confirmPasswordTextField;
    @FXML private Label errorLabel;

    // =========================================================================
    // Initialization
    // =========================================================================

    /** Constructs the controller and sets the authentication strategy. */
    public SignUpController() {
        authEngine.setAuthStrategy(new AppAuth());
    }

    /**
     * Initializes the form.  Binds the visible text fields to their hidden
     * PasswordField counterparts for bidirectional text synchronization.
     */
    @FXML
    private void initialize() {
        passwordTextField.setManaged(passwordTextField.isVisible());
        confirmPasswordTextField.setManaged(confirmPasswordTextField.isVisible());
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        confirmPasswordTextField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
    }

    // =========================================================================
    // Account Creation
    // =========================================================================

    /**
     * Validates the form inputs and creates a new account.
     * Checks for valid email format, minimum password length,
     * alphanumeric password content, and matching confirmation.
     * On success the session is initialized and the dashboard is shown.
     */
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

    /**
     * Maps a Message flag to a user-facing error string and shows it.
     * @param flag the result message from the authentication engine
     */
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

    // =========================================================================
    // Navigation
    // =========================================================================

    /** Navigates back to the login screen. */
    @FXML
    private void handleSignIn() {
        clearError();
        App.showLogin();
    }

    // =========================================================================
    // UI Helpers
    // =========================================================================

    /** Toggles the visibility of the password field between text and masked. */
    @FXML
    private void handleTogglePassword() {
        boolean showing = passwordTextField.isVisible();
        passwordTextField.setVisible(!showing);
        passwordTextField.setManaged(!showing);
        passwordField.setVisible(showing);
        passwordField.setManaged(showing);
    }

    /** Toggles the visibility of the confirm password field between text and masked. */
    @FXML
    private void handleToggleConfirmPassword() {
        boolean showing = confirmPasswordTextField.isVisible();
        confirmPasswordTextField.setVisible(!showing);
        confirmPasswordTextField.setManaged(!showing);
        confirmPasswordField.setVisible(showing);
        confirmPasswordField.setManaged(showing);
    }

    /**
     * Displays an error message on the form.
     * @param message the error text to show
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /** Hides the error label. */
    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}