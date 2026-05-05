package com.duck;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Region;

public class GoalCardController {

    @FXML
    private Label categoryLabel;

    @FXML
    private Label spentLabel;

    @FXML
    private Label limitLabel;

    @FXML
    private Label utilizedLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label daysRemainingLabel;

    @FXML
    private Region statusBorder;

    public void setCardData(String category, double spent, double limit, int daysRemaining) {
        categoryLabel.setText(category.toUpperCase());
        spentLabel.setText(String.format("$%,.0f", spent));
        limitLabel.setText(String.format("/ $%,.0f", limit));
        daysRemainingLabel.setText(daysRemaining + " days remaining in cycle");

        double percentage = limit > 0 ? (spent / limit) : 0;
        progressBar.setProgress(percentage);
        
        int percentInt = (int) (percentage * 100);
        utilizedLabel.setText(percentInt + "% Utilized");

        updateStatusStyles(percentage);
    }

    private void updateStatusStyles(double percentage) {
        // Clear previous styles
        progressBar.getStyleClass().removeAll("progress-green", "progress-yellow", "progress-red");
        statusBorder.getStyleClass().removeAll("border-green", "border-yellow", "border-red");
        statusLabel.getStyleClass().removeAll("status-on-track", "status-approaching", "status-action-required");

        if (percentage < 0.75) {
            statusLabel.setText("On Track");
            statusLabel.getStyleClass().add("status-on-track");
            progressBar.getStyleClass().add("progress-green");
            statusBorder.getStyleClass().add("border-green");
        } else if (percentage < 0.90) {
            statusLabel.setText("Approaching Limit");
            statusLabel.getStyleClass().add("status-approaching");
            progressBar.getStyleClass().add("progress-yellow");
            statusBorder.getStyleClass().add("border-yellow");
        } else {
            statusLabel.setText("Action Required");
            statusLabel.getStyleClass().add("status-action-required");
            progressBar.getStyleClass().add("progress-red");
            statusBorder.getStyleClass().add("border-red");
        }
    }
}
