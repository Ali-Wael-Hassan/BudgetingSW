package com.duck.model.type;

public class AppSettings {
    public enum DataKey {
        ACCOUNTS,
        EXPENSES,
        INCOME,
        BUDGETS,
        CATEGORIES,
        GOALS
    }

    public enum Mode {
        DARK,
        LIGHT
    }

    public enum Currency {
        EGP,
        USD,
        EUR
    }

    public enum TransactionType {
        EXPENSE,
        INCOME
    }

    public enum Message {
        ERROR,
        SUCCESS,
        NOT_FOUND,
        NULL_ACCOUNT,
        ACCOUNT_MISMATCH_ERROR,
        INVALID_EMAIL,
        // Goals Specific Flags
        NULL_GOAL_ERROR,
        INVALID_NAME,
        INVALID_TARGET_AMOUNT,
        NEGATIVE_CURRENT_AMOUNT,
        CURRENT_EXCEEDS_TARGET,
        NULL_DEADLINE,
        PAST_DEADLINE,
        MULTIPLE_ACTIVE_GOALS_ERROR,
        // Budgets Specific Flags
        NULL_BUDGET_ERROR,
        INVALID_CATEGORY,
        INVALID_BUDGET_AMOUNT,
        NEGATIVE_USED_AMOUNT,
        NULL_PERIOD,
        INVALID_PERIOD,
        INVALID_THRESHOLD,
        MULTIPLE_ACTIVE_BUDGETS_ERROR,
        // Transaction Specific Flags
        NULL_TRANSACTION_ERROR,
        INVALID_TRANSACTION_AMOUNT,
        INVALID_DATE,

    }

    public enum GoalEvent {
        GOAL_COMPLETED("goalCompleted");
    
        private final String propertyName;
    
        GoalEvent(String propertyName) {
            this.propertyName = propertyName;
        }
    
        public String getName() {
            return propertyName;
        }
    }

    public enum AccountEvent {
        TOKEN_CHANGED("token");

        private final String propertyName;
    
        AccountEvent(String propertyName) {
            this.propertyName = propertyName;
        }
    
        public String getName() {
            return propertyName;
        }
    }

    public enum TransactionEvent {
        TRANSACTION_RECEIVED("transaction");

        private final String propertyName;
    
        TransactionEvent(String propertyName) {
            this.propertyName = propertyName;
        }
    
        public String getName() {
            return propertyName;
        }
    }

    public enum BudgetEvent {
        TRANSACTION_RECEIVED("transaction"),
        THRESHOLD_REACHED("budgetThresholdReached"),
        BUDGET_EXCEEDED("budgetExceeded"),
        BUDGET_UPDATED("budgetUpdated");
    
        private final String propertyName;
    
        BudgetEvent(String propertyName) {
            this.propertyName = propertyName;
        }
    
        public String getName() {
            return propertyName;
        }
    }
}
