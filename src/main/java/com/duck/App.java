package com.duck;

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

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        Parent root = loadFXML("sign_up");
        scene = new Scene(root, 960, 720);
        scene.getStylesheets().add(App.class.getResource("styles.css").toExternalForm());
        stage.setTitle("BudgetWise");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Switch to Sign-Up screen
     */
    public static void showSignUp() {
        try {
            Parent newRoot = loadFXML("sign_up");
            scene.setRoot(newRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Switch to Login screen
     */
    public static void showLogin() {
        try {
            Parent newRoot = loadFXML("login");
            scene.setRoot(newRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Switch to Dashboard screen
     */
    public static void showDashboard() {
        try {
            System.out.println("Loading dashboard...");
            setRoot("dashboard");
            System.out.println("Dashboard loaded successfully");
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Switch to Transactions screen
     */
    public static void showTransactions() {
        try {
            setRoot("transactions");
        } catch (IOException e) {
            System.out.println("ERROR loading transactions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Switch to Budgets screen
     */
    public static void showBudgets() {
        try {
            setRoot("budget");
        } catch (IOException e) {
            System.out.println("ERROR loading budget: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Switch to Saving Goals screen
     */
    public static void showGoals() {
        try {
            setRoot("goals");
        } catch (IOException e) {
            System.out.println("ERROR loading goals: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Switch to Reports screen
     */
    public static void showReports() {
        try {
            setRoot("reports");
        } catch (IOException e) {
            System.out.println("ERROR loading reports: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Switch to Profile screen
     */
    public static void showProfile() {
        try {
            setRoot("profile");
        } catch (IOException e) {
            System.out.println("ERROR loading profile: " + e.getMessage());
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

