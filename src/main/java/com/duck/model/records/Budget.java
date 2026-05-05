package com.duck.model.records;

import java.time.Period;

public class Budget {
    private String category;
    private int amount;
    private int usedAmount;
    private Period period; 
    private float threshold;

    public Budget(String category, int amount, Period period, float threshold) {
        this.category = category;
        this.amount = amount;
        this.usedAmount = 0; // Initially, no amount is used
        this.period = period;
        this.threshold = threshold;
    }

    public String getCategory() {
        return category;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public int getUsedAmount() {
        return usedAmount;
    }
    
    public Period getPeriod() {
        return period;
    }
    
    public float getThreshold() {
        return threshold;
    }
}
