package com.duck;

import javafx.fxml.FXML;

public class TransactionsController {

    @FXML
    private void initialize() {
        System.out.println("Transactions page loaded");
    }

    @FXML
    private void navigateToDashboard() {
        App.showDashboard();
    }

    @FXML
    private void navigateToTransactions() {
        System.out.println("Already on Transactions");
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

    @FXML
    private void filterAll() {
        System.out.println("Filter: All");
    }

    @FXML
    private void filterIncome() {
        System.out.println("Filter: Income");
    }

    @FXML
    private void filterExpense() {
        System.out.println("Filter: Expense");
    }
}