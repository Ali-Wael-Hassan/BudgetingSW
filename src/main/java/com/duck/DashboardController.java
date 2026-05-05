package com.duck;

import javafx.fxml.FXML;

public class DashboardController {

    @FXML
    private void initialize() {
        System.out.println("Dashboard loaded");
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
        App.showReports();
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