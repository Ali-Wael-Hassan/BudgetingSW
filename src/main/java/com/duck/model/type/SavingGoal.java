package com.duck.model.type;

import java.time.LocalDate;

public class SavingGoal {
    private String name;
    private float targetAmount;
    private float currentAmount;
    private LocalDate deadline;
    private Account account;

    public SavingGoal() {}

    public SavingGoal(String name, float targetAmount, float currentAmount, LocalDate deadline, Account account) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.deadline = deadline;
        this.account = account;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || getClass() != obj.getClass()) return false;

        SavingGoal other = (SavingGoal) obj;

        if (this.name == null) {
            return other.name == null;
        }
        return this.name.equalsIgnoreCase(other.name);
    }

    public Account getAccount() {
        return this.account;
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
    
    public void setAccount(Account account) {
        this.account = account;
    }

    public float getRemainingAmount() {
        return targetAmount - currentAmount;
    }

    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return !this.getDeadline().isBefore(today) && this.getCurrentAmount() < this.getTargetAmount();
    }
}