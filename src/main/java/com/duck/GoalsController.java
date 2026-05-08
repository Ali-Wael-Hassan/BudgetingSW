package com.duck;

import com.duck.model.records.SavingGoalsManager;
import com.duck.model.type.Account;
import com.duck.model.type.AppSettings.Message;
import com.duck.model.type.SavingGoal;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * FXML controller for the Goals screen.  Loads and displays saving goal
 * cards, listens for data model changes, and handles new goal creation
 * with validation and error reporting.
 */
public class GoalsController implements Initializable, PropertyChangeListener {

    // =========================================================================
    // FXML Controls
    // =========================================================================

    @FXML private FlowPane cardsContainer;
    @FXML private StackPane sidebarAvatarContainer;

    // =========================================================================
    // Instance State
    // =========================================================================

    private final ApplicationState state = ApplicationState.getInstance();
    private final SavingGoalsManager goalsController = state.getGoalsManager();
    private Account currentAccount;

    // =========================================================================
    // Initialization
    // =========================================================================

    /**
     * Initializes the controller.  Registers a property change listener on
     * the goals manager and schedules a full refresh.
     * @param location  unused
     * @param resources unused
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        goalsController.addPropertyChangeListener(this);
        Platform.runLater(this::refresh);
    }

    /**
     * Fired when the underlying goals data changes.  Schedules a UI refresh
     * on the JavaFX application thread.
     * @param evt unused
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        javafx.application.Platform.runLater(() -> {
            System.out.println("Data changed! Refreshing UI...");
            refresh();
        });
    }

    /** Rebuilds all goal cards with the latest data and applies the current theme. */
    private void refresh() {
        currentAccount = state.getCurrentAccount();
        if (currentAccount == null) return;
        AvatarHelper.setSidebarAvatar(sidebarAvatarContainer, currentAccount);
        applyTheme();
        renderCards();
    }

    /** Applies the account's preferred color theme. */
    private void applyTheme() {
        if (currentAccount == null) return;
        com.duck.model.type.AppSettings.Mode mode = com.duck.model.type.AppSettings.Mode.DARK;
        if (currentAccount.getAccountConfig() != null && currentAccount.getAccountConfig().getMode() != null) {
            mode = currentAccount.getAccountConfig().getMode();
        }
        App.setTheme(mode);
    }

    // =========================================================================
    // Rendering
    // =========================================================================

    /** Loads each saving goal as a goal_card.fxml node and adds it to the container. */
    private void renderCards() {
        cardsContainer.getChildren().clear();
        List<SavingGoal> goals = goalsController.getAllSavings(currentAccount);

        if (goals.isEmpty()) {
            return;
        }

        for (SavingGoal goal : goals) {
            try {
                URL fxmlUrl = getClass().getResource("/com/duck/goal_card.fxml");
                if (fxmlUrl == null) {
                    throw new IllegalStateException("goal_card.fxml resource not found");
                }

                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Node card = loader.load();
                GoalCardController cardCtrl = loader.getController();
                cardCtrl.setGoal(goal);
                cardsContainer.getChildren().add(card);

            } catch (IOException e) {
                throw new RuntimeException("Failed to load goal card FXML", e);
            }
        }
    }

    // =========================================================================
    // Actions
    // =========================================================================

    /** Opens the new-category dialog. */
    @FXML
    private void handleAddCategory() {
        DialogHelper.showNewCategoryDialog();
    }

    /**
     * Opens the new goal dialog and creates the goal if validation passes.
     * Displays a specific error alert for each validation failure.
     */
    @FXML
    private void handleNewGoal() {
        currentAccount = state.getCurrentAccount();
        if (currentAccount == null) return;

        SavingGoal goal = DialogHelper.showGoalDialog(currentAccount);
        if (goal != null) {
            Message result = goalsController.createSavingGoal(goal);
            if (result == Message.SUCCESS) {
                renderCards();
            } else {
                String error;
                switch (result) {
                    case INVALID_NAME: error = "Please enter a valid goal name."; break;
                    case INVALID_TARGET_AMOUNT: error = "Target amount must be greater than zero."; break;
                    case NEGATIVE_CURRENT_AMOUNT: error = "Current amount cannot be negative."; break;
                    case CURRENT_EXCEEDS_TARGET: error = "Current amount cannot exceed the target amount."; break;
                    case NULL_DEADLINE: error = "Please select a deadline."; break;
                    case PAST_DEADLINE: error = "Deadline must be in the future."; break;
                    case MULTIPLE_ACTIVE_GOALS_ERROR: error = "Only one active saving goal is allowed at a time."; break;
                    default: error = "Failed to create goal. Please try again.";
                }
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Validation Error");
                alert.setHeaderText(null);
                alert.setContentText(error);
                alert.getDialogPane().getStylesheets().add(
                        getClass().getResource("styles.css").toExternalForm());
                alert.showAndWait();
            }
        }
    }

    // =========================================================================
    // Screen Navigation
    // =========================================================================

    /** Navigates to the Dashboard screen. */
    @FXML private void navigateToDashboard() { App.showDashboard(); }
    /** Navigates to the Transactions screen. */
    @FXML private void navigateToTransactions() { App.showTransactions(); }
    /** Navigates to the Budgets screen. */
    @FXML private void navigateToBudgets() { App.showBudgets(); }
    /** Navigates to the Goals screen. */
    @FXML private void navigateToGoals() { App.showGoals(); }
    /** Navigates to the Reports screen. */
    @FXML private void navigateToReports() { App.showReports(); }
    /** Navigates to the Profile screen. */
    @FXML private void navigateToProfile() { App.showProfile(); }
}