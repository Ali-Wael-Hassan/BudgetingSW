package com.duck;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import com.duck.model.type.SavingGoal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class GoalCardController {

    @FXML private AnchorPane  cardRoot;
    @FXML private Label       categoryLabel;
    @FXML private Label       spentLabel;
    @FXML private Label       limitLabel;
    @FXML private Label       utilizedLabel;
    @FXML private Label       statusLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label       daysRemainingLabel;
    @FXML private Region      statusBorder;

    public void setGoal(SavingGoal goal) {
        // Map model fields to UI labels using getters from SavingGoal.java
        categoryLabel.setText(goal.getName().toUpperCase());
        spentLabel.setText(String.format("$%.2f", goal.getCurrentAmount()));
        limitLabel.setText(String.format("of $%.2f", goal.getTargetAmount()));

        // Calculate progress percentage 
        float progress = 0;
        if (goal.getTargetAmount() > 0) {
            progress = goal.getCurrentAmount() / goal.getTargetAmount();
        }
        
        progressBar.setProgress(Math.min(progress, 1.0));
        utilizedLabel.setText((int)(progress * 100) + "% Utilized");

        // Calculate days remaining until deadline 
        if (goal.getDeadline() != null) {
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), goal.getDeadline());
            daysRemainingLabel.setText(Math.max(0, daysLeft) + " days remaining");
        }

        updateStyles(goal, progress);
    }

    private void updateStyles(SavingGoal goal, float progress) {
        statusLabel.getStyleClass().removeAll("card-status-text-green", "card-status-text-yellow", "card-status-text-red");
        statusBorder.getStyleClass().removeAll("status-border-green", "status-border-yellow", "status-border-red");

        if (progress >= 1.0) {
            statusLabel.setText("COMPLETED");
            statusLabel.getStyleClass().add("card-status-text-green");
            statusBorder.getStyleClass().add("status-border-green");
        } else if (!goal.isActive()) {
            statusLabel.setText("EXPIRED");
            statusLabel.getStyleClass().add("card-status-text-red");
            statusBorder.getStyleClass().add("status-border-red");
        } else {
            statusLabel.setText("ON TRACK");
            statusLabel.getStyleClass().add("card-status-text-green");
            statusBorder.getStyleClass().add("status-border-green");
        }
    }
}