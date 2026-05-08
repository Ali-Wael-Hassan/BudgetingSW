package com.duck.model.records;

import java.util.ArrayList;
import java.util.List;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.duck.model.dataAccessors.LocalStorage;
import com.duck.model.type.Account;
import com.duck.model.type.Period;
import com.duck.model.type.Transaction;
import com.duck.model.type.AppSettings.BudgetEvent;
import com.duck.model.type.AppSettings.DataKey;
import com.duck.model.type.AppSettings.Message;
import com.duck.model.type.AppSettings.TransactionType;
import com.duck.model.type.AppSettings.AccountEvent;
import com.duck.model.type.AppSettings.TransactionEvent;

public class BudgetManager implements PropertyChangeListener {
    private List<Budget> budgets = new ArrayList<>();
    private final PropertyChangeSupport support;

    public BudgetManager() {
        support =  new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    private Message validateBudget(Budget budget) {
    
        // 1. Check for null object
        if (budget == null) {
            return Message.NULL_BUDGET_ERROR;
        }
    
        // 2. Validate Category (Cannot be null or empty)
        if (budget.getCategory() == null || budget.getCategory().trim().isEmpty()) {
            return Message.INVALID_CATEGORY;
        }
    
        // 3. Validate Amount (Total budget limit must be greater than zero)
        if (budget.getAmount() <= 0) {
            return Message.INVALID_BUDGET_AMOUNT;
        }
    
        // 4. Validate Used Amount (Cannot be negative)
        if (budget.getUsedAmount() < 0) {
            return Message.NEGATIVE_USED_AMOUNT;
        }
    
        // 5. Validate Period (Cannot be null, zero, or negative)
        if (budget.getPeriod() == null) {
            return Message.NULL_PERIOD;
        }
        if (budget.getPeriod().getEndDate().isBefore(budget.getPeriod().getStartDate())) {
            return Message.INVALID_PERIOD;
        }
    
        // 6. Validate Threshold (Cannot be negative or zero)
        if (budget.getThreshold() <= 0) {
            return Message.INVALID_THRESHOLD;
        }
    
        // 7. Validate Account (A budget must be linked to an account)
        if (budget.getAccount() == null) {
            return Message.NULL_ACCOUNT;
        }

        // 8. No two budgets can have the same category in overlapping periods (per account)
        for (Budget existingBudget : budgets) {
            if (existingBudget.getAccount().equals(budget.getAccount())) {
                if (existingBudget.getCategory().equalsIgnoreCase(budget.getCategory())) {
                    Period ep = existingBudget.getPeriod();
                    Period bp = budget.getPeriod();
                    if (ep != null && bp != null && periodsOverlap(ep, bp)) {
                        return Message.MULTIPLE_ACTIVE_BUDGETS_ERROR;
                    }
                }
            }
        }
    
        return Message.SUCCESS;
    }

    private boolean periodsOverlap(Period a, Period b) {
        return !a.getEndDate().isBefore(b.getStartDate()) && !b.getEndDate().isBefore(a.getStartDate());
    }

    public Message createBudget(Budget budget) {
        Message check = validateBudget(budget);
        if (check == Message.SUCCESS) {
            budgets.add(budget);

            if(!LocalStorage.getInstance().getCategories().contains(budget.getCategory()))
                LocalStorage.getInstance().insert(DataKey.CATEGORIES, budget.getCategory());

            LocalStorage.getInstance().save(DataKey.BUDGETS, new ArrayList<>(budgets));
            support.firePropertyChange(BudgetEvent.BUDGET_UPDATED.getName(), null, budget);
            return Message.SUCCESS;
        }
        return check;
    }

    public Message editBudget(Budget budget, Budget updatedBudget) {
        if (budget == null || updatedBudget == null) {
            return Message.NULL_BUDGET_ERROR; 
        }
    
        // 1. The account must remain exactly the same
        if (budget.getAccount() == null || !budget.getAccount().equals(updatedBudget.getAccount())) {
            return Message.ACCOUNT_MISMATCH_ERROR;
        }
    
        // 2. Find the original budget in the list
        int index = budgets.indexOf(budget);
        if (index == -1) {
            return Message.NOT_FOUND;
        }
    
        // 3. Temporarily remove the old budget. 
        budgets.remove(index);
    
        // 4. Validate the new updated budget
        Message validationResult = validateBudget(updatedBudget);
    
        // 5. Apply the result
        if (validationResult == Message.SUCCESS) {
            budgets.add(index, updatedBudget);

            LocalStorage.getInstance().save(DataKey.BUDGETS, new ArrayList<>(budgets));
            
            support.firePropertyChange(BudgetEvent.BUDGET_UPDATED.getName(), budget, updatedBudget);
            
            return Message.SUCCESS;
        } else {
            budgets.add(index, budget);
            return validationResult;
        }
    }

    public List<Budget> getAllBudgets(Account account) {
        List<Budget> accountBudgets = new ArrayList<>();
    
        for (Budget budget : budgets) {
            if (budget.getAccount() != null && budget.getAccount().equals(account)) {
                accountBudgets.add(budget);
            }
        }
        return accountBudgets;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("BudgetController: Property '" + evt.getPropertyName() + "' changed.");

        if (TransactionEvent.TRANSACTION_RECEIVED.getName().equals(evt.getPropertyName())) {
            Object newValue = evt.getNewValue();
        
            if (newValue instanceof Transaction) {
                Transaction transaction = (Transaction) newValue;
                handleTransactionUpdate(transaction);
            }
        }

        if (AccountEvent.TOKEN_CHANGED.getName().equals(evt.getPropertyName())) {
            String newToken = (String) evt.getNewValue();
            if (newToken != null) {
                this.budgets = LocalStorage.getInstance().getBudgets();
            } else if (this.budgets != null) {
                this.budgets.clear();
            }
        }
    }

    private void handleTransactionUpdate(Transaction transaction) {
        System.out.println("Processing transaction: " + transaction);
    
        // 1. Get the category of the transaction 
        String transactionCategory = transaction.getCategory(); 
        Budget activeBudget = null;
    
        // 2. Find the active budget matching the category
        for (Budget budget : budgets) {
            if (budget.getCategory().equalsIgnoreCase(transactionCategory) && budget.isActive()) {
                activeBudget = budget;
                break;
            }
        }
    
        // 3. If no budget exists for this category, exit early
        if (activeBudget == null) {
            System.out.println("No active budget found for category: " + transactionCategory);
            return;
        }
    
        // 4. Extract the exact transaction amount. 
        float amount = transaction.getAmount();
        float currentUsedAmount = activeBudget.getUsedAmount();
        float newUsedAmount = currentUsedAmount;
    
        // 5. Add or subtract based on whether it is an Income or Expense
        if (transaction.getType() == TransactionType.EXPENSE) {
            newUsedAmount = currentUsedAmount + amount;
        } else if (transaction.getType() == TransactionType.INCOME) {
            newUsedAmount = currentUsedAmount - amount;
        }
    
        // Prevent negative balance
        if (newUsedAmount < 0) {
            newUsedAmount = 0;
        }
    
        activeBudget.setUsedAmount(newUsedAmount); 

        LocalStorage.getInstance().save(DataKey.BUDGETS, new ArrayList<>(budgets));
        
        System.out.println("Transaction applied to " + activeBudget.getCategory() + ". Total used: " + newUsedAmount);
    
        // 6. Check Limits (Exceeded vs Warning)
        if (activeBudget.getUsedAmount() >= activeBudget.getAmount()) {
            System.out.println("Alert! The budget for '" + activeBudget.getCategory() + "' has been exceeded!");
            support.firePropertyChange(BudgetEvent.BUDGET_EXCEEDED.getName(), null, activeBudget);
            
        } else if (activeBudget.getUsedAmount() >= activeBudget.getThreshold() * activeBudget.getAmount()) {
            System.out.println("Warning! The budget for '" + activeBudget.getCategory() + "' has reached its threshold!");
            support.firePropertyChange(BudgetEvent.THRESHOLD_REACHED.getName(), null, activeBudget);
        }
    }
}
