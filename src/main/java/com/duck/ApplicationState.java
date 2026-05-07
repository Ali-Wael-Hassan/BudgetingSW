package com.duck;

import com.duck.model.accountOps.AccountManager;
import com.duck.model.authentication.Session;
import com.duck.model.dataAccessors.LocalStorage;
import com.duck.model.records.Budget;
import com.duck.model.records.BudgetController;
import com.duck.model.records.SavingGoalsController;
import com.duck.model.records.TransactionManager;
import com.duck.model.type.Account;
import com.duck.model.type.AppSettings.BudgetEvent;
import com.duck.model.type.AppSettings.GoalEvent;
import com.duck.model.type.SavingGoal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class ApplicationState {

    private static ApplicationState instance;

    private final LocalStorage storage;
    private final TransactionManager transactionManager;
    private final BudgetController budgetController;
    private final SavingGoalsController goalsController;
    private final AccountManager accountManager;

    private ApplicationState() {
        this.storage = LocalStorage.getInstance();
        this.transactionManager = new TransactionManager();
        this.budgetController = new BudgetController();
        this.goalsController = new SavingGoalsController();
        this.accountManager = new AccountManager();

        registerListeners();

        String existingToken = Session.getInstance().getToken();
        if (existingToken != null && !existingToken.isEmpty()) {
            broadcastToken(existingToken);
        }
    }

    public static ApplicationState getInstance() {
        if (instance == null) {
            instance = new ApplicationState();
        }
        return instance;
    }

    private void registerListeners() {
        budgetController.addPropertyChangeListener(this::onBudgetEvent);
        goalsController.addPropertyChangeListener(this::onGoalEvent);
        transactionManager.addPropertyChangeListener(budgetController);
        transactionManager.addPropertyChangeListener(goalsController);
    }

    public void addTransactionListener(PropertyChangeListener listener) {
        transactionManager.addPropertyChangeListener(listener);
    }

    private void onBudgetEvent(java.beans.PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();
        if (BudgetEvent.BUDGET_EXCEEDED.getName().equals(prop)) {
            Budget budget = (Budget) evt.getNewValue();
            if (budget != null) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Budget Exceeded");
                    alert.setHeaderText("Budget exceeded for '" + budget.getCategory() + "'");
                    alert.setContentText("The budget for '" + budget.getCategory() + "' has been exceeded! Used: "
                            + String.format("%.2f", budget.getUsedAmount()) + " / Limit: "
                            + String.format("%.2f", budget.getAmount()));
                    DialogHelper.styleDialogButtons(alert);
                    DialogHelper.addThemeStylesheets(alert);
                    alert.show();
                });
            }
        } else if (BudgetEvent.THRESHOLD_REACHED.getName().equals(prop)) {
            Budget budget = (Budget) evt.getNewValue();
            if (budget != null) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Budget Threshold Reached");
                    alert.setHeaderText("Warning: Budget threshold reached for '" + budget.getCategory() + "'");
                    alert.setContentText("The budget for '" + budget.getCategory() + "' has reached its threshold! Used: "
                            + String.format("%.2f", budget.getUsedAmount()) + " / Limit: "
                            + String.format("%.2f", budget.getAmount()));
                    DialogHelper.styleDialogButtons(alert);
                    DialogHelper.addThemeStylesheets(alert);
                    alert.show();
                });
            }
        }
    }

    private void onGoalEvent(java.beans.PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();
        if (GoalEvent.GOAL_COMPLETED.getName().equals(prop)) {
            SavingGoal goal = (SavingGoal) evt.getNewValue();
            if (goal != null) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Goal Completed");
                    alert.setHeaderText("Congratulations!");
                    alert.setContentText("The saving goal '" + goal.getName() + "' is fully funded!");
                    DialogHelper.styleDialogButtons(alert);
                    DialogHelper.addThemeStylesheets(alert);
                    alert.show();
                });
            }
        }
    }

    public void initializeSession(String email) {
        String token = System.currentTimeMillis() + "_" + email;
        Session.getInstance().saveToken(token);
        broadcastToken(token);
    }

    public void clearSession() {
        Session.getInstance().saveToken(null);
        broadcastToken(null);
    }

    private void broadcastToken(String token) {
        PropertyChangeEvent evt = new PropertyChangeEvent(this, "token", null, token);
        budgetController.propertyChange(evt);
        goalsController.propertyChange(evt);
        accountManager.propertyChange(evt);
        transactionManager.propertyChange(evt);
    }

    public LocalStorage getStorage() {
        return storage;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public BudgetController getBudgetController() {
        return budgetController;
    }

    public SavingGoalsController getGoalsController() {
        return goalsController;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public Account getCurrentAccount() {
        String token = Session.getInstance().getToken();
        if (token == null) return null;
        try {
            String[] parts = token.split("_");
            String email = parts.length >= 2 ? parts[1] : null;
            if (email == null) return null;
            for (Account acc : storage.getAccounts()) {
                if (acc.getEmail().equalsIgnoreCase(email)) return acc;
            }
        } catch (Exception ignored) {}
        return null;
    }
}
