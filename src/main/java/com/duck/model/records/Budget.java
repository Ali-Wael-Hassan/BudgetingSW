package com.duck.model.records;

import java.time.LocalDate;

import com.duck.model.type.Period;

import com.duck.model.type.Account;

/**
 * Represents a budget for a specific category within a date period.
 * Tracks the total limit, the amount already spent, an alert threshold
 * percentage, and the owning Account.  A budget is considered active
 * when the current date falls within its period.
 */
public class Budget {
    private String category;
    private float amount;
    private float usedAmount;
    private Period period; 
    private float threshold;
    private Account account;

    /** Constructs an empty Budget. */
    public Budget() {}

    /**
     * Constructs a Budget with the given values.
     * @param category the budget category
     * @param amount   the total spending limit
     * @param period   the date range for this budget
     * @param threshold the alert threshold fraction (e.g. 0.75 for 75%)
     */
    public Budget(String category, float amount, Period period, float threshold) {
        this.category = category;
        this.amount = amount;
        this.usedAmount = 0;
        this.period = period;
        this.threshold = threshold;
    }

    /** @return the owning account */
    public Account getAccount() {
        return this.account;
    }

    /** @return the category name */
    public String getCategory() {
        return category;
    }
    
    /** @return the total spending limit */
    public float getAmount() {
        return amount;
    }
    
    /** @return the amount already spent */
    public float getUsedAmount() {
        return usedAmount;
    }
    
    /** @return the date period */
    public Period getPeriod() {
        return period;
    }
    
    /** @return the alert threshold fraction */
    public float getThreshold() {
        return threshold;
    }

    /** @param category the new category name */
    public void setCategory(String category) {
        this.category = category;
    }
    
    /** @param amount the new spending limit */
    public void setAmount(float amount) {
        this.amount = amount;
    }
    
    /** @param amount the new used amount */
    public void setUsedAmount(float amount) {
        this.usedAmount = amount;
    }
    
    /** @param period the new date period */
    public void setPeriod(Period period) {
        this.period = period;
    }
    
    /** @param threshold the new alert threshold */
    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    /** @param account the new owning account */
    public void setAccount(Account account) {
        this.account = account;
    }

    /**
     * Determines whether this budget is currently active.
     * A budget is active if today falls within its period.
     * @return true if the budget period contains today's date
     */
    public boolean isActive() {
        return period.contains(LocalDate.now());
    }
}
