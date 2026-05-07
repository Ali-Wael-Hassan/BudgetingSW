package com.duck;

import com.duck.model.accountOps.AccountManager;
import com.duck.model.authentication.Session;
import com.duck.model.dataAccessors.LocalStorage;
import com.duck.model.records.BudgetController;
import com.duck.model.records.SavingGoalsController;
import com.duck.model.records.TransactionManager;

import java.beans.PropertyChangeEvent;

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

    private void onBudgetEvent(java.beans.PropertyChangeEvent evt) {
    }

    private void onGoalEvent(java.beans.PropertyChangeEvent evt) {
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
}
