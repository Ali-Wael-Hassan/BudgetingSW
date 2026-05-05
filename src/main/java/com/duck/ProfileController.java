package com.duck;

import javafx.fxml.FXML;

public class ProfileController {

    @FXML
    private void initialize() {
        System.out.println("Profile page loaded");
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
        App.showReports();
    }

    @FXML
    private void navigateToProfile() {
        System.out.println("Already on Profile");
    }

    @FXML
    private void handleAddTransaction() {
        System.out.println("Add Transaction clicked");
    }

    @FXML
    private void handleSignOut() {
        System.out.println("Sign out clicked");
        App.showLogin();
    }
}