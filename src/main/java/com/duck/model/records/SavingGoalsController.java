package com.duck.model.records;

import com.duck.model.type.Account;
import com.duck.model.type.SavingGoal;
import com.duck.model.type.Transaction;

public class SavingGoalsController implements IObserver {
    
    private void validateGoal(SavingGoal savingGoal) {
        /* Logic */
    }

    public void createSavingGoal(SavingGoal savingGoal) {
        // if (validateGoal(savingGoal)) {
        //     /* Logic */
        // }
    }

    public int calculateMonthlySaving(SavingGoal savingGoal) {
        return 0;
    }

    public SavingGoal[] getAllSavings(Account account) {
        return new SavingGoal[0];
    }

    @Override
    public void update(Transaction transaction) {
        System.out.println("SavingGoalsController: Checking if transaction affects goals...");
        // Logic to update saving goals based on the new transaction
    }

}