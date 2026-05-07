package com.duck;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

public class GoalCardController {

    // ── FXML injections — match fx:id in goal_card.fxml ───────────
    @FXML private AnchorPane  cardRoot;
    @FXML private Label       categoryLabel;
    @FXML private Label       spentLabel;
    @FXML private Label       limitLabel;
    @FXML private Label       utilizedLabel;
    @FXML private Label       statusLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label       daysRemainingLabel;
    @FXML private Region      statusBorder;

    // ── Public API called by GoalsController ───────────────────────

    /**
     * Populates every field of this card from the given Goal model.
     * Called by GoalsController right after FXMLLoader.load().
     */
    public void setGoal(GoalsController.Goal goal) {
        // Category (shown in UPPERCASE)
        categoryLabel.setText(goal.category.toUpperCase());

        // Spent / limit amounts
        spentLabel.setText(String.format("$%.0f", goal.spent));
        limitLabel.setText(String.format("/ $%.0f", goal.limit));

        // Progress bar — clamped to [0, 1] so it never visually overflows
        double progress = Math.min(goal.percent(), 1.0);
        progressBar.setProgress(progress);

        // Utilized percentage (can exceed 100% if over budget)
        int pct = (int) Math.round(goal.percent() * 100);
        utilizedLabel.setText(pct + "% Utilized");

        // Status text
        statusLabel.setText(goal.statusText());

        // Style the status text and left border based on status
        statusLabel.getStyleClass().removeAll(
                "card-status-text-green",
                "card-status-text-yellow",
                "card-status-text-red");

        statusBorder.getStyleClass().removeAll(
                "status-border-green",
                "status-border-yellow",
                "status-border-red");

        if (goal.isOver()) {
            statusLabel.getStyleClass().add("card-status-text-red");
            statusBorder.getStyleClass().add("status-border-red");
        } else if (goal.isWarn()) {
            statusLabel.getStyleClass().add("card-status-text-yellow");
            statusBorder.getStyleClass().add("status-border-yellow");
        } else {
            statusLabel.getStyleClass().add("card-status-text-green");
            statusBorder.getStyleClass().add("status-border-green");
        }

        // Days remaining
        daysRemainingLabel.setText(goal.daysRemaining + " days remaining in cycle");
    }
}
