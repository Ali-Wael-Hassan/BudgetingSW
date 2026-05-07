package com.duck;

import com.duck.model.authentication.Session;
import com.duck.model.type.AppSettings;

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
 * JavaFX App with Scene Transitions
 */
public class App extends Application {

    private static Scene scene;
    private static Stage primaryStage;
    private static AppSettings.Mode currentTheme = AppSettings.Mode.DARK;
    private static String lightThemeCss;

    public static void setTheme(AppSettings.Mode mode) {
        currentTheme = mode;
        if (scene == null) return;
        if (lightThemeCss == null) {
            java.net.URL url = App.class.getResource("theme-light.css");
            if (url != null) lightThemeCss = url.toExternalForm();
        }
        if (mode == AppSettings.Mode.LIGHT && lightThemeCss != null) {
            if (!scene.getStylesheets().contains(lightThemeCss)) {
                scene.getStylesheets().add(lightThemeCss);
            }
        } else if (lightThemeCss != null) {
            scene.getStylesheets().remove(lightThemeCss);
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        String token = Session.getInstance().getToken();
        Parent root;
        if (token != null && !token.isEmpty()) {
            root = loadFXML("dashboard");
        } else {
            root = loadFXML("login");
        }
        scene = new Scene(root, 960, 720);
        scene.getStylesheets().add(App.class.getResource("styles.css").toExternalForm());
        stage.setTitle("BudgetWise");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Switch to Sign-Up screen with fade and slide animation
     */
    public static void showSignUp() {
        try {
            Parent newRoot = loadFXML("sign_up");
            animateSceneTransition(newRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Switch to Login screen with fade and slide animation
     */
    public static void showLogin() {
        try {
            Parent newRoot = loadFXML("login");
            animateSceneTransition(newRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Switch to Dashboard screen with fade and slide animation
     */
    public static void showDashboard() {
        try {
            Parent newRoot = loadFXML("dashboard");
            animateSceneTransition(newRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Switch to Transactions screen
     */
    public static void showTransactions() {
        try {
            Parent newRoot = loadFXML("transactions");
            animateSceneTransition(newRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showBudgets() {
        try {
            Parent newRoot = loadFXML("budget");
            animateSceneTransition(newRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showGoals() {
        try {
            Parent newRoot = loadFXML("goals");
            animateSceneTransition(newRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showReports() {
        try {
            Parent newRoot = loadFXML("reports");
            animateSceneTransition(newRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showProfile() {
        try {
            Parent newRoot = loadFXML("profile");
            animateSceneTransition(newRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Animate scene transition using Fade and Slide effects
     */
    private static void animateSceneTransition(Parent newRoot) {
        Parent currentRoot = scene.getRoot();

        // Fade out current root
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), currentRoot);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        // Slide out current root to the left
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), currentRoot);
        slideOut.setFromX(0);
        slideOut.setToX(-100);

        // New root starts off-screen to the right
        newRoot.setTranslateX(100);
        newRoot.setOpacity(0);

        // Fade in new root
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newRoot);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        // Slide in new root from the right
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), newRoot);
        slideIn.setFromX(100);
        slideIn.setToX(0);

        // Combine animations
        ParallelTransition outTransition = new ParallelTransition(fadeOut, slideOut);
        ParallelTransition inTransition = new ParallelTransition(fadeIn, slideIn);

        outTransition.setOnFinished(event -> {
            scene.setRoot(newRoot);
            inTransition.play();
        });

        outTransition.play();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        java.net.URL url = App.class.getResource(fxml + ".fxml");
        if (url == null) {
            throw new IOException("Resource not found: " + fxml + ".fxml");
        }
        fxmlLoader.setLocation(url);
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        try {
            launch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

