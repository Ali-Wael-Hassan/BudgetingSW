package com.duck.model.type;

import java.time.LocalDate;

public class Transaction {

    private TransactionConfig config;
    private LocalDate date;
    private float amount;

    public Transaction(TransactionConfig config, LocalDate date, long amount) {
        this.config = config;
        this.date = date;
        this.amount = amount;
    }

    public Account getAccount() {
        return this.config.getAccount();
    }

    public TransactionConfig getConfig() {
        return this.config;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public float getAmount() {
        return this.amount;
    }
    
    public AppSettings.TransactionType getType() {
        return config.getType();
    }

    public String getCategory() {
        return config.getCategory().get(0);
    }

    public void setAccount(Account account) {
        this.config.setAccount(account);
    }
}