package com.duck.model.records;

import com.duck.model.type.Account;
import com.duck.model.type.Transaction;
import com.duck.model.type.AppSettings.Message;

public class BudgetController implements IObserver {

    private Message validateBudget(Budget budget) {
        /* Logic */
        return Message.SUCCESS;
    }
    

    public Budget createBudget(Budget budget) {
        if (validateBudget(budget) == Message.SUCCESS) {
            /* Logic */
            return budget;
        }
        return null;
    }

    public Message editBudget(Budget budget, Budget updatedBudget) {
        return Message.SUCCESS;
    }

    public Budget[] getAllBudgets(Account account) {
        return new Budget[0];
    }

    @Override
    public void update(Transaction transaction) {
        System.out.println("BudgetController: Updating budget spend for " + transaction.getCategory());
        // Logic to subtract transaction amount from remaining budget
    }
}
