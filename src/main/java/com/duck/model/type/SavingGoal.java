package com.duck.model.type;

import java.time.LocalDate;

public class SavingGoal {
    private String name;
    private float targetAmount;
    private float currentAmount;
    private LocalDate deadline;

    public SavingGoal(String name, float targetAmount, float currentAmount, LocalDate deadline) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.deadline = deadline;
    }

    public String getName() {
        return this.name;
    }

    public float getTargetAmount() {
        return this.targetAmount;
    }

    public float getCurrentAmount() {
        return this.currentAmount;
    }

    public LocalDate getDeadline() {
        return this.deadline;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTargetAmount(float targetAmount) {
        this.targetAmount = targetAmount;
    }

    public void setCurrentAmount(float currentAmount) {
        this.currentAmount = currentAmount;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public float getRemainingAmount() {
        return targetAmount - currentAmount;
    }
}