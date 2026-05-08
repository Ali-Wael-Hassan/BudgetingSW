package com.duck.model.type;

import java.time.LocalDate;

/**
 * Represents a single financial transaction (income or expense).
 * Stores a TransactionConfig (which holds type, category, period, range,
 * and account), a date, and the monetary amount.
 */
public class Transaction {

    private TransactionConfig config;
    private LocalDate date;
    private float amount;

    /** Constructs an empty Transaction. */
    public Transaction() {}

    /**
     * Constructs a Transaction with the given values.
     * @param config the transaction configuration (type, category, account, etc.)
     * @param date   the transaction date
     * @param amount the transaction amount
     */
    public Transaction(TransactionConfig config, LocalDate date, float amount) {
        this.config = config;
        this.date = date;
        this.amount = amount;
    }

    /** @return the account associated with this transaction */
    public Account getAccount() {
        return this.config.getAccount();
    }

    /** @return the full transaction configuration */
    public TransactionConfig getConfig() {
        return this.config;
    }

    /** @return the transaction date */
    public LocalDate getDate() {
        return this.date;
    }

    /** @return the transaction amount */
    public float getAmount() {
        return this.amount;
    }
    
    /** @return the transaction type (INCOME or EXPENSE) */
    public AppSettings.TransactionType getType() {
        return config.getType();
    }

    /** @return the first category from the config */
    public String getCategory() {
        return config.getCategory().get(0);
    }

    /** @param account the new account to associate */
    public void setAccount(Account account) {
        this.config.setAccount(account);
    }
}