package com.duck;

import com.duck.model.accountOps.AccountManager;
import com.duck.model.authentication.Session;
import com.duck.model.dataAccessors.LocalStorage;
import com.duck.model.records.Budget;
import com.duck.model.records.BudgetManager;
import com.duck.model.records.SavingGoalsManager;
import com.duck.model.records.TransactionManager;
import com.duck.model.type.Account;
import com.duck.model.type.AppSettings.BudgetEvent;
import com.duck.model.type.AppSettings.GoalEvent;
import com.duck.model.type.SavingGoal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 * Singleton that owns the primary model managers (TransactionManager,
 * BudgetManager, SavingGoalsManager, AccountManager) and coordinates
 * event propagation across the application.  Registers internal listeners
 * for budget threshold/exceeded events and goal completion events to
 * show alert dialogs.
 */
public class ApplicationState {

    // =========================================================================
    // Singleton
    // =========================================================================

    private static ApplicationState instance;

    // =========================================================================
    // Model Managers
    // =========================================================================

    private final LocalStorage storage;
    private final TransactionManager transactionManager;
    private final BudgetManager budgetManager;
    private final SavingGoalsManager goalsManager;
    private final AccountManager accountManager;

    // =========================================================================
    // Initialization
    // =========================================================================

    /** Constructs all model managers and registers internal event listeners. */
    private ApplicationState() {
        this.storage = LocalStorage.getInstance();
        this.transactionManager = new TransactionManager();
        this.budgetManager = new BudgetManager();
        this.goalsManager = new SavingGoalsManager();
        this.accountManager = new AccountManager();

        registerListeners();

        String existingToken = Session.getInstance().getToken();
        if (existingToken != null && !existingToken.isEmpty()) {
            broadcastToken(existingToken);
        }
    }

    /**
     * Returns the singleton ApplicationState instance, creating it if necessary.
     * @return the shared ApplicationState
     */
    public static ApplicationState getInstance() {
        if (instance == null) {
            instance = new ApplicationState();
        }
        return instance;
    }

    /** Wires up internal PropertyChangeListeners between managers. */
    private void registerListeners() {
        budgetManager.addPropertyChangeListener(this::onBudgetEvent);
        goalsManager.addPropertyChangeListener(this::onGoalEvent);
        transactionManager.addPropertyChangeListener(budgetManager);
        transactionManager.addPropertyChangeListener(goalsManager);
    }

    /**
     * Registers an external listener for transaction changes.
     * @param listener the listener to add
     */
    public void addTransactionListener(PropertyChangeListener listener) {
        transactionManager.addPropertyChangeListener(listener);
    }

    // =========================================================================
    // Event Handlers
    // =========================================================================

    /**
     * Handles budget events.  Shows a warning alert when a budget is
     * exceeded or reaches its configured threshold.
     * @param evt the budget property change event
     */
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

    /**
     * Handles goal events.  Shows an information alert when a saving goal
     * is fully funded.
     * @param evt the goal property change event
     */
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

    // =========================================================================
    // Session Management
    // =========================================================================

    /**
     * Initializes a new session for the given email and broadcasts the token
     * to all managers.
     * @param email the account email to associate with the session
     */
    public void initializeSession(String email) {
        String token = System.currentTimeMillis() + "_" + email;
        Session.getInstance().saveToken(token);
        broadcastToken(token);
    }

    /** Clears the current session and broadcasts a null token to all managers. */
    public void clearSession() {
        Session.getInstance().saveToken(null);
        broadcastToken(null);
    }

    /**
     * Broadcasts a session token (or null) to all model managers so they
     * can reload their data.
     * @param token the session token, or null to clear
     */
    private void broadcastToken(String token) {
        PropertyChangeEvent evt = new PropertyChangeEvent(this, "token", null, token);
        budgetManager.propertyChange(evt);
        goalsManager.propertyChange(evt);
        accountManager.propertyChange(evt);
        transactionManager.propertyChange(evt);
    }

    // =========================================================================
    // Accessors
    // =========================================================================

    /** @return the LocalStorage instance for data persistence */
    public LocalStorage getStorage() {
        return storage;
    }

    /** @return the TransactionManager for transaction operations */
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    /** @return the BudgetManager for budget operations */
    public BudgetManager getBudgetManager() {
        return budgetManager;
    }

    /** @return the SavingGoalsManager for saving goal operations */
    public SavingGoalsManager getGoalsManager() {
        return goalsManager;
    }

    /** @return the AccountManager for account operations */
    public AccountManager getAccountManager() {
        return accountManager;
    }

    /**
     * Resolves and returns the current Account from the active session token.
     * @return the current Account, or null if no valid session exists
     */
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
