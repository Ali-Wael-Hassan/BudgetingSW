package com.duck.model.type;

import java.time.LocalDate;

/**
 * Represents a user-defined saving goal.  Tracks the target amount,
 * current progress, deadline, and the owning Account.  Provides
 * methods to check remaining amount and whether the goal is still
 * active (not past deadline and not yet fully funded).
 */
public class SavingGoal {
    private String name;
    private float targetAmount;
    private float currentAmount;
    private LocalDate deadline;
    private Account account;

    /** Constructs an empty SavingGoal. */
    public SavingGoal() {}

    /**
     * Constructs a SavingGoal with the given values.
     * @param name          the goal name
     * @param targetAmount  the target amount to save
     * @param currentAmount the amount currently saved
     * @param deadline      the target deadline
     * @param account       the owning account
     */
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

    /** @return the owning account */
    public Account getAccount() {
        return this.account;
    }

    /** @return the goal name */
    public String getName() {
        return this.name;
    }

    /** @return the target amount */
    public float getTargetAmount() {
        return this.targetAmount;
    }

    /** @return the current saved amount */
    public float getCurrentAmount() {
        return this.currentAmount;
    }

    /** @return the deadline */
    public LocalDate getDeadline() {
        return this.deadline;
    }

    /** @param name the new goal name */
    public void setName(String name) {
        this.name = name;
    }

    /** @param targetAmount the new target amount */
    public void setTargetAmount(float targetAmount) {
        this.targetAmount = targetAmount;
    }

    /** @param currentAmount the new current amount */
    public void setCurrentAmount(float currentAmount) {
        this.currentAmount = currentAmount;
    }

    /** @param deadline the new deadline */
    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
    
    /** @param account the new owning account */
    public void setAccount(Account account) {
        this.account = account;
    }

    /** @return the remaining amount needed to reach the target */
    public float getRemainingAmount() {
        return targetAmount - currentAmount;
    }

    /**
     * Determines whether this goal is still active.
     * A goal is active if its deadline has not passed and the current
     * amount has not yet reached the target.
     * @return true if the goal is still in progress
     */
    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return !this.getDeadline().isBefore(today) && this.getCurrentAmount() < this.getTargetAmount();
    }
}