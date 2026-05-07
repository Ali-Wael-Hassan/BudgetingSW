package com.duck;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import com.duck.model.records.SavingGoalsController;
import com.duck.model.type.SavingGoal;

public class GoalsController {

    @FXML private FlowPane cardsContainer;
    @FXML private VBox filterPanel;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;

    private final SavingGoalsController savingGoalsController = new SavingGoalsController();

    @FXML
    public void initialize() {
        refreshGoals();
    }

    @FXML
    private void handleFilter() {
        boolean isVisible = filterPanel.isVisible();
        filterPanel.setVisible(!isVisible);
        filterPanel.setManaged(!isVisible);
    }

    @FXML
    private void handleApplyFilter() {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();

        List<SavingGoal> goals = savingGoalsController.getAllGoals();

        List<SavingGoal> filtered = goals.stream()
            .filter(goal -> {
                LocalDate deadline = goal.getDeadline();
                if (deadline == null) return false;
                if (from != null && deadline.isBefore(from)) return false;
                if (to != null && deadline.isAfter(to)) return false;
                return true;
            })
            .collect(Collectors.toList());

        renderGoals(filtered);
    }

    @FXML
    private void handleClearFilter() {
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        refreshGoals();
        // Optionally close the panel after clearing
        filterPanel.setVisible(false);
        filterPanel.setManaged(false);
    }

    @FXML
    private void handleAddGoal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("add_goal.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Add New Saving Goal");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(loader.load()));
            stage.showAndWait();
            refreshGoals();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml")); // ← your actual dashboard fxml
            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshGoals() {
        List<SavingGoal> goals = savingGoalsController.getAllGoals();
        renderGoals(goals);
    }

    private void renderGoals(List<SavingGoal> goals) {
        cardsContainer.getChildren().clear();
        for (SavingGoal goal : goals) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("goal_card.fxml"));
                Node goalNode = loader.load();
                GoalCardController controller = loader.getController();
                controller.setGoal(goal);
                cardsContainer.getChildren().add(goalNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}