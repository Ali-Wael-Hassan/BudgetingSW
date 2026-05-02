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
        NOT_FOUND
    }
}
