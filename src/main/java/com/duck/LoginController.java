package com.duck;

import com.duck.model.authentication.AppAuth;
import com.duck.model.authentication.Recognition;
import com.duck.model.authentication.Login;
import com.duck.model.type.Account;
import com.duck.model.type.AppSettings.Message;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * FXML controller for the Login screen.  Validates credentials, authenticates
 * the user, and navigates to the dashboard on success.  Also manages the
 * password visibility toggle and the link to the sign up screen.
 */
public class LoginController {

    private Recognition authEngine = new Login();

    // =========================================================================
    // FXML Controls
    // =========================================================================

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private Label errorLabel;
    @FXML private Hyperlink signUpLink;

    // =========================================================================
    // Initialization
    // =========================================================================

    /** Constructs the controller and sets the authentication strategy. */
    public LoginController() {
        authEngine.setAuthStrategy(new AppAuth());
    }

    /**
     * Initializes the form.  Binds the visible text field to the hidden
     * PasswordField for bidirectional text synchronization.
     */
    @FXML
    private void initialize() {
        if (passwordTextField != null && passwordField != null) {
            passwordTextField.setManaged(passwordTextField.isVisible());
            passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        }
    }

    // =========================================================================
    // Authentication
    // =========================================================================

    /**
     * Validates the email and password inputs and attempts to sign in.
     * On success the session is initialized and the dashboard is shown.
     */
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

    /**
     * Maps a Message flag to a user-facing error string and shows it.
     * @param flag the result message from the authentication engine
     */
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

    // =========================================================================
    // Navigation
    // =========================================================================

    /** Navigates to the sign up screen. */
    @FXML
    private void handleSignUp() {
        clearError();
        App.showSignUp();
    }

    // =========================================================================
    // UI Helpers
    // =========================================================================

    /** Toggles the visibility of the password field between text and masked. */
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