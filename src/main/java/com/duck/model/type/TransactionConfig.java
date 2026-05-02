package com.duck.model.type;

public class TransactionConfig {
    private AppSettings.TransactionType type;
    private Period period;
    private String category;
    private Range range;

    public TransactionConfig(AppSettings.TransactionType type, Period period, String category, Range range) {
        this.type = type;
        this.period = period;
        this.category = category;
        this.range = range;
    }

    public AppSettings.TransactionType getType() {
        return this.type;
    }

    public Period getPeriod() {
        return this.period;
    }

    public String getCategory() {
        return this.category;
    }

    public Range getRange() {
        return this.range;
    }

    public void setType(AppSettings.TransactionType type) {
        this.type = type;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setRange(Range range) {
        this.range = range;
    }
}