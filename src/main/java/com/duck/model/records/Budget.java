package com.duck.model.records;

import java.time.LocalDate;

import com.duck.model.type.Period;

import com.duck.model.type.Account;

public class Budget {
    private String category;
    private float amount;
    private float usedAmount;
    private Period period; 
    private float threshold;
    private Account account;

    public Budget() {}

    public Budget(String category, float amount, Period period, float threshold) {
        this.category = category;
        this.amount = amount;
        this.usedAmount = 0;
        this.period = period;
        this.threshold = threshold;
    }

    public Account getAccount() {
        return this.account;
    }

    public String getCategory() {
        return category;
    }
    
    public float getAmount() {
        return amount;
    }
    
    public float getUsedAmount() {
        return usedAmount;
    }
    
    public Period getPeriod() {
        return period;
    }
    
    public float getThreshold() {
        return threshold;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    
    public void setAmount(float amount) {
        this.amount = amount;
    }
    
    public void setUsedAmount(float amount) {
        this.usedAmount = amount;
    }
    
    public void setPeriod(Period period) {
        this.period = period;
    }
    
    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public boolean isActive() {
        return period.contains(LocalDate.now());
    }
}
