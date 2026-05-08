package com.duck;

import com.duck.model.authentication.Session;
import com.duck.model.type.AppSettings.Mode;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Main JavaFX Application class.  Initializes the primary Stage and
 * Scene, manages theme stylesheet toggling, and provides static
 * navigation methods that animate transitions between FXML screens.
 */
public class App extends Application {

    // =========================================================================
    // Scene and Theme State
    // =========================================================================

    private static Scene scene;
    private static String lightThemeCss;

    /**
     * Toggles the light or dark theme by adding or removing the light
     * theme stylesheet from the scene.
     * @param mode the desired theme mode (LIGHT or DARK)
     */
    public static void setTheme(Mode mode) {
        if (scene == null) return;
        if (lightThemeCss == null) {
            java.net.URL url = App.class.getResource("theme-light.css");
            if (url != null) lightThemeCss = url.toExternalForm();
        }
        if (mode == Mode.LIGHT && lightThemeCss != null) {
            if (!scene.getStylesheets().contains(lightThemeCss)) {
                scene.getStylesheets().add(lightThemeCss);
            }
        } else if (lightThemeCss != null) {
            scene.getStylesheets().remove(lightThemeCss);
        }
    }

    // =========================================================================
    // Application Lifecycle
    // =========================================================================

    /**
     * Starts the JavaFX application.  Checks for an existing session token
     * to decide whether to show the dashboard or the login screen.
     * @param stage the primary stage for the application
     * @throws IOException if the initial FXML resource cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        String token = Session.getInstance().getToken();
        Parent root;
        if (token != null && !token.isEmpty()) {
            root = loadFXML("dashboard");
        } else {
            root = loadFXML("login");
        }
        scene = new Scene(root, 960, 720);
        scene.getStylesheets().add(App.class.getResource("styles.css").toExternalForm());
        stage.setTitle("BudgetingSW");
        stage.setScene(scene);
        stage.show();
    }

    // =========================================================================
    // Screen Navigation
    // =========================================================================

    /** Navigates to the Sign Up screen with a fade and slide animation. */
    public static void showSignUp() {
        try {
            Parent newRoot = loadFXML("sign_up");
            animateSceneTransition(newRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Navigates to the Login screen with a fade and slide animation. */
    public static void showLogin() {
        try {
            Parent newRoot = loadFXML("login");
            animateSceneTransition(newRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Navigates to the Dashboard screen with a fade and slide animation. */
    public static void showDashboard() {
        try {
            Parent newRoot = loadFXML("dashboard");
            animateSceneTransition(newRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Navigates to the Transactions screen with a fade and slide animation. */
    public static void showTransactions() {
        try {
            Parent newRoot = loadFXML("transactions");
            animateSceneTransition(newRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Navigates to the Budgets screen with a fade and slide animation. */
    public static void showBudgets() {
        try {
            Parent newRoot = loadFXML("budget");
            animateSceneTransition(newRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Navigates to the Goals screen with a fade and slide animation. */
    public static void showGoals() {
        try {
            Parent newRoot = loadFXML("goals");
            animateSceneTransition(newRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Navigates to the Reports screen with a fade and slide animation. */
    public static void showReports() {
        try {
            Parent newRoot = loadFXML("reports");
            animateSceneTransition(newRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Navigates to the Profile screen with a fade and slide animation. */
    public static void showProfile() {
        try {
            Parent newRoot = loadFXML("profile");
            animateSceneTransition(newRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    // Animation
    // =========================================================================

    /**
     * Animates the scene root transition with a parallel fade-out / slide-out
     * of the current root, followed by a fade-in / slide-in of the new root.
     * @param newRoot the new scene root to transition to
     */
    private static void animateSceneTransition(Parent newRoot) {
        Parent currentRoot = scene.getRoot();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), currentRoot);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), currentRoot);
        slideOut.setFromX(0);
        slideOut.setToX(-100);

        newRoot.setTranslateX(100);
        newRoot.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newRoot);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), newRoot);
        slideIn.setFromX(100);
        slideIn.setToX(0);

        ParallelTransition outTransition = new ParallelTransition(fadeOut, slideOut);
        ParallelTransition inTransition = new ParallelTransition(fadeIn, slideIn);

        outTransition.setOnFinished(event -> {
            scene.setRoot(newRoot);
            inTransition.play();
        });

        outTransition.play();
    }

    // =========================================================================
    // FXML Loading
    // =========================================================================

    /**
     * Replaces the scene root with the given FXML file (no animation).
     * @param fxml the FXML file name without extension
     * @throws IOException if the resource cannot be loaded
     */
    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    /**
     * Loads an FXML file from the classpath resources.
     * @param fxml the FXML file name without extension
     * @return the loaded Parent node
     * @throws IOException if the resource is not found or cannot be loaded
     */
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        java.net.URL url = App.class.getResource(fxml + ".fxml");
        if (url == null) {
            throw new IOException("Resource not found: " + fxml + ".fxml");
        }
        fxmlLoader.setLocation(url);
        return fxmlLoader.load();
    }

    // =========================================================================
    // Entry Point
    // =========================================================================

    /**
     * Application entry point.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            launch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

