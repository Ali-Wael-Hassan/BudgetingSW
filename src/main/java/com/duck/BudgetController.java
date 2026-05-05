package com.duck;

import javafx.fxml.FXML;

public class BudgetController {

    @FXML
    private void initialize() {
        System.out.println("Budget page loaded");
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
        System.out.println("Already on Budgets");
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

    @FXML
    private void handleCreateBudget() {
        System.out.println("Create Budget clicked");
    }
}