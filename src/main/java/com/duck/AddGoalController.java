package com.duck;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import com.duck.model.records.SavingGoalsController;
import com.duck.model.type.SavingGoal;
import java.time.LocalDate;

public class AddGoalController {
    @FXML private TextField categoryField;
    @FXML private TextField limitField;
    @FXML private DatePicker deadlinePicker;

    private final SavingGoalsController savingGoalsController = new SavingGoalsController();

    @FXML
    private void handleSave() {
        // Reset styles
        categoryField.setStyle("");
        limitField.setStyle("");

        boolean valid = true;

        String name = categoryField.getText().trim();
        if (name.isEmpty()) {
            categoryField.setStyle("-fx-border-color: #F87171;");
            valid = false;
        }

        float target = 0;
        try {
            target = Float.parseFloat(limitField.getText().trim());
        } catch (NumberFormatException e) {
            limitField.setStyle("-fx-border-color: #F87171;");
            valid = false;
        }

        LocalDate deadline = deadlinePicker.getValue();
        if (deadline == null) {
            deadlinePicker.setStyle("-fx-border-color: #F87171;");
            valid = false;
        }

        if (!valid) return;

        try {
            SavingGoal newGoal = new SavingGoal(name, target, 0, deadline, null);
            savingGoalsController.createSavingGoal(newGoal);
            closeStage();
        } catch (Exception e) {
            e.printStackTrace(); // Check your console for the real error
        }
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) categoryField.getScene().getWindow();
        stage.close();
    }
}