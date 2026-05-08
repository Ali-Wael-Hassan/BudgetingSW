package com.duck;

import com.duck.model.records.SavingGoalsController;
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

public class GoalsController implements Initializable, PropertyChangeListener {

    @FXML private FlowPane cardsContainer;
    @FXML private StackPane sidebarAvatarContainer;

    private final ApplicationState state = ApplicationState.getInstance();
    private final SavingGoalsController goalsController = state.getGoalsController();
    private Account currentAccount;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        goalsController.addPropertyChangeListener(this);
        Platform.runLater(this::refresh);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        javafx.application.Platform.runLater(() -> {
            System.out.println("Data changed! Refreshing UI...");
            refresh();
        });
    }

    private void refresh() {
        currentAccount = state.getCurrentAccount();
        if (currentAccount == null) return;
        AvatarHelper.setSidebarAvatar(sidebarAvatarContainer, currentAccount);
        applyTheme();
        renderCards();
    }

    private void applyTheme() {
        if (currentAccount == null) return;
        com.duck.model.type.AppSettings.Mode mode = com.duck.model.type.AppSettings.Mode.DARK;
        if (currentAccount.getAccountConfig() != null && currentAccount.getAccountConfig().getMode() != null) {
            mode = currentAccount.getAccountConfig().getMode();
        }
        App.setTheme(mode);
    }

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

    @FXML
    private void handleAddCategory() {
        DialogHelper.showNewCategoryDialog();
    }

    @FXML private void navigateToDashboard() { App.showDashboard(); }
    @FXML private void navigateToTransactions() { App.showTransactions(); }
    @FXML private void navigateToBudgets() { App.showBudgets(); }
    @FXML private void navigateToGoals() { App.showGoals(); }
    @FXML private void navigateToReports() { App.showReports(); }
    @FXML private void navigateToProfile() { App.showProfile(); }

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
}