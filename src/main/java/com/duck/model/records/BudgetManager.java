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

/**
 * Manages all Budget objects for the application.  Supports creating,
 * editing, and querying budgets per account.  Listens for
 * TransactionEvent to track spending against active budgets and fires
 * BudgetEvent alerts when thresholds or limits are exceeded.
 */
public class BudgetManager implements PropertyChangeListener {
    private List<Budget> budgets = new ArrayList<>();
    private final PropertyChangeSupport support;

    /** Constructs an empty BudgetManager with a PropertyChangeSupport. */
    public BudgetManager() {
        support =  new PropertyChangeSupport(this);
    }

    /** @param listener the listener to add */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /** @param listener the listener to remove */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    /**
     * Validates a budget against all business rules.
     * @param budget the budget to validate
     * @return SUCCESS or the appropriate error Message
     */
    private Message validateBudget(Budget budget) {
        if (budget == null) {
            return Message.NULL_BUDGET_ERROR;
        }
        if (budget.getCategory() == null || budget.getCategory().trim().isEmpty()) {
            return Message.INVALID_CATEGORY;
        }
        if (budget.getAmount() <= 0) {
            return Message.INVALID_BUDGET_AMOUNT;
        }
        if (budget.getUsedAmount() < 0) {
            return Message.NEGATIVE_USED_AMOUNT;
        }
        if (budget.getPeriod() == null) {
            return Message.NULL_PERIOD;
        }
        if (budget.getPeriod().getEndDate().isBefore(budget.getPeriod().getStartDate())) {
            return Message.INVALID_PERIOD;
        }
        if (budget.getThreshold() <= 0) {
            return Message.INVALID_THRESHOLD;
        }
        if (budget.getAccount() == null) {
            return Message.NULL_ACCOUNT;
        }
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

    /**
     * Checks whether two Periods overlap.
     * @param a first period
     * @param b second period
     * @return true if the periods overlap
     */
    private boolean periodsOverlap(Period a, Period b) {
        return !a.getEndDate().isBefore(b.getStartDate()) && !b.getEndDate().isBefore(a.getStartDate());
    }

    /**
     * Creates a new budget after validation.  Persists to storage and
     * fires a BUDGET_UPDATED event on success.
     * @param budget the budget to create
     * @return SUCCESS or the validation error Message
     */
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

    /**
     * Edits an existing budget by replacing it with an updated version
     * after re-validation.  The account must remain unchanged.
     * @param budget        the original budget to replace
     * @param updatedBudget the new budget data
     * @return SUCCESS or the appropriate error Message
     */
    public Message editBudget(Budget budget, Budget updatedBudget) {
        if (budget == null || updatedBudget == null) {
            return Message.NULL_BUDGET_ERROR; 
        }
        if (budget.getAccount() == null || !budget.getAccount().equals(updatedBudget.getAccount())) {
            return Message.ACCOUNT_MISMATCH_ERROR;
        }
        int index = budgets.indexOf(budget);
        if (index == -1) {
            return Message.NOT_FOUND;
        }
        budgets.remove(index);
        Message validationResult = validateBudget(updatedBudget);
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

    /**
     * Returns all budgets belonging to the given account.
     * @param account the account to filter by
     * @return list of matching budgets
     */
    public List<Budget> getAllBudgets(Account account) {
        List<Budget> accountBudgets = new ArrayList<>();
        for (Budget budget : budgets) {
            if (budget.getAccount() != null && budget.getAccount().equals(account)) {
                accountBudgets.add(budget);
            }
        }
        return accountBudgets;
    }

    /**
     * Handles property change events.  Responds to TRANSACTION_RECEIVED
     * by updating budget used amounts, and to TOKEN_CHANGED by loading
     * or clearing persisted budget data.
     * @param evt the property change event
     */
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

    /**
     * Applies a transaction to the matching active budget.  Adds expense
     * amounts to usedAmount and subtracts income.  Fires alert events
     * when the budget exceeds its limit or reaches the threshold.
     * @param transaction the transaction to apply
     */
    private void handleTransactionUpdate(Transaction transaction) {
        System.out.println("Processing transaction: " + transaction);
        String transactionCategory = transaction.getCategory(); 
        Budget activeBudget = null;

        for (Budget budget : budgets) {
            if (budget.getCategory().equalsIgnoreCase(transactionCategory) && budget.isActive()) {
                activeBudget = budget;
                break;
            }
        }

        if (activeBudget == null) {
            System.out.println("No active budget found for category: " + transactionCategory);
            return;
        }

        float amount = transaction.getAmount();
        float currentUsedAmount = activeBudget.getUsedAmount();
        float newUsedAmount = currentUsedAmount;

        if (transaction.getType() == TransactionType.EXPENSE) {
            newUsedAmount = currentUsedAmount + amount;
        } else if (transaction.getType() == TransactionType.INCOME) {
            newUsedAmount = currentUsedAmount - amount;
        }

        if (newUsedAmount < 0) {
            newUsedAmount = 0;
        }

        activeBudget.setUsedAmount(newUsedAmount); 
        LocalStorage.getInstance().save(DataKey.BUDGETS, new ArrayList<>(budgets));
        System.out.println("Transaction applied to " + activeBudget.getCategory() + ". Total used: " + newUsedAmount);

        if (activeBudget.getUsedAmount() >= activeBudget.getAmount()) {
            System.out.println("Alert! The budget for '" + activeBudget.getCategory() + "' has been exceeded!");
            support.firePropertyChange(BudgetEvent.BUDGET_EXCEEDED.getName(), null, activeBudget);
        } else if (activeBudget.getUsedAmount() >= activeBudget.getThreshold() * activeBudget.getAmount()) {
            System.out.println("Warning! The budget for '" + activeBudget.getCategory() + "' has reached its threshold!");
            support.firePropertyChange(BudgetEvent.THRESHOLD_REACHED.getName(), null, activeBudget);
        }
    }
}
