package com.duck;

import javafx.fxml.FXML;

public class ReportsController {

    @FXML
    private void initialize() {
        System.out.println("Reports page loaded");
    }

    @FXML
    private void navigateToDashboard() {
        App.showDashboard();
    }

    @FXML
    private void navigateToTransactions() {
        App.showTransactions();
    }

    @FXML
    private void navigateToBudgets() {
        App.showBudgets();
    }

    @FXML
    private void navigateToGoals() {
        App.showGoals();
    }

    @FXML
    private void navigateToReports() {
        System.out.println("Already on Reports");
    }

    @FXML
    private void navigateToProfile() {
        App.showProfile();
    }

    @FXML
    private void handleAddTransaction() {
        System.out.println("Add Transaction clicked");
    }
}