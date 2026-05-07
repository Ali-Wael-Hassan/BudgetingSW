package com.duck;

import com.duck.model.type.SavingGoal;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class GoalCardController {

    @FXML private AnchorPane  cardRoot;
    @FXML private Label       goalNameLabel;
    @FXML private Label       currentLabel;
    @FXML private Label       targetLabel;
    @FXML private Label       progressLabel;
    @FXML private Label       statusLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label       daysLabel;
    @FXML private Region      statusBorder;

    public void setGoal(SavingGoal goal) {
        if (goal == null) return;

        goalNameLabel.setText(goal.getName());

        currentLabel.setText(String.format("$%,.0f", goal.getCurrentAmount()));
        targetLabel.setText(String.format("/ $%,.0f", goal.getTargetAmount()));

        double pct = goal.getTargetAmount() > 0
                ? Math.min(goal.getCurrentAmount() / goal.getTargetAmount(), 1.0)
                : 0;
        progressBar.setProgress(pct);
        progressLabel.setText((int) (pct * 100) + "%");

        long daysRemaining = goal.getDeadline() != null
                ? ChronoUnit.DAYS.between(LocalDate.now(), goal.getDeadline())
                : 0;
        boolean completed = goal.getCurrentAmount() >= goal.getTargetAmount();
        boolean overdue = !completed && daysRemaining < 0;

        String statusText;
        String statusColor;
        String borderColor;

        if (completed) {
            statusText = "Completed";
            statusColor = "card-status-text-green";
            borderColor = "status-border-green";
            daysLabel.setText("Goal achieved!");
        } else if (overdue) {
            statusText = "Overdue";
            statusColor = "card-status-text-red";
            borderColor = "status-border-red";
            daysLabel.setText("Past deadline");
        } else {
            double timeElapsed = goal.getDeadline() != null
                    ? 1.0 - (daysRemaining / Math.max(ChronoUnit.DAYS.between(goal.getDeadline().minusMonths(1), goal.getDeadline()), 1))
                    : 0;
            if (pct < timeElapsed * 0.75 && daysRemaining < 30) {
                statusText = "Behind";
                statusColor = "card-status-text-yellow";
                borderColor = "status-border-yellow";
            } else {
                statusText = "On Track";
                statusColor = "card-status-text-green";
                borderColor = "status-border-green";
            }
            daysLabel.setText(daysRemaining + " days remaining");
        }

        statusLabel.setText(statusText);
        statusLabel.getStyleClass().removeAll("card-status-text-green", "card-status-text-yellow", "card-status-text-red");
        statusLabel.getStyleClass().add(statusColor);

        statusBorder.getStyleClass().removeAll("status-border-green", "status-border-yellow", "status-border-red");
        statusBorder.getStyleClass().add(borderColor);
    }
}