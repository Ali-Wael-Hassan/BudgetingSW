package com.duck.model.records;

import java.util.ArrayList;
import java.util.List;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.duck.model.dataAccessors.LocalStorage;
import com.duck.model.type.Account;
import com.duck.model.type.SavingGoal;
import com.duck.model.type.Transaction;
import com.duck.model.type.AppSettings.DataKey;
import com.duck.model.type.AppSettings.GoalEvent;
import com.duck.model.type.AppSettings.Message;

public class SavingGoalsController implements PropertyChangeListener {
    private List<SavingGoal> goals = new ArrayList<>();
    private final PropertyChangeSupport support;
    
    public SavingGoalsController() {
        support =  new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    private Message validateGoal(SavingGoal savingGoal) {

        // 1. Check for null object
        if (savingGoal == null) {
            return Message.NULL_GOAL_ERROR; 
        }

        // 2. Validate Name (Cannot be null or empty)
        if (savingGoal.getName() == null || savingGoal.getName().trim().isEmpty()) {
            return Message.INVALID_NAME;
        }

        // 3. Validate Target Amount (Must be greater than zero)
        if (savingGoal.getTargetAmount() <= 0) {
            return Message.INVALID_TARGET_AMOUNT;
        }

        // 4. Validate Current Amount (Cannot be negative)
        if (savingGoal.getCurrentAmount() < 0) {
            return Message.NEGATIVE_CURRENT_AMOUNT;
        }

        // 5. Current amount shouldn't exceed the target amount
        if (savingGoal.getCurrentAmount() > savingGoal.getTargetAmount()) {
            return Message.CURRENT_EXCEEDS_TARGET;
        }

        // 6. Validate Deadline (Must exist and be in the future/present)
        if (savingGoal.getDeadline() == null) {
            return Message.NULL_DEADLINE;
        }
        if (savingGoal.getDeadline().isBefore(LocalDate.now())) {
            return Message.PAST_DEADLINE;
        }
        
        // 7. Only ONE active saving goal allowed at a time
        
        for (SavingGoal existingGoal : goals) {
            if (existingGoal.isActive()) {
                return Message.MULTIPLE_ACTIVE_GOALS_ERROR; 
            }
        }

        // 7. Validate Account (A budget must be linked to an account)
        if (savingGoal.getAccount() == null) {
            return Message.NULL_ACCOUNT;
        }

        return Message.SUCCESS;
    }

    public Message createSavingGoal(SavingGoal savingGoal) {
        Message check = validateGoal(savingGoal);
        if (check == Message.SUCCESS) {
            goals.add(savingGoal);
            LocalStorage.getInstance().save(DataKey.GOALS, new ArrayList<>(goals));
            return Message.SUCCESS;
        }

        return check;
    }

    public float calculateMonthlySaving(SavingGoal savingGoal) {
        if (savingGoal == null) {
            return 0;
        }

        float remainingAmount = savingGoal.getRemainingAmount();

        if (remainingAmount <= 0) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        long monthsRemaining = ChronoUnit.MONTHS.between(today, savingGoal.getDeadline());

        if (monthsRemaining <= 0) {
            return remainingAmount;
        }

        return remainingAmount / monthsRemaining;
    }

    public List<SavingGoal> getAllSavings(Account account) {
        List<SavingGoal> accountGoals = new ArrayList<>();
    
        for (SavingGoal goal : goals) {
            if (goal.getAccount() != null && goal.getAccount().equals(account)) {
                accountGoals.add(goal);
            }
        }
        return accountGoals;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("SavingGoalsController: Property '" + evt.getPropertyName() + "' changed.");

        if ("transaction".equals(evt.getPropertyName())) {
            Object newValue = evt.getNewValue();
        
            if (newValue instanceof Transaction) {
                Transaction transaction = (Transaction) newValue;
                handleTransactionUpdate(transaction);
            }
        }

        if ("token".equals(evt.getPropertyName())) {
            String newToken = (String) evt.getNewValue();
            if (newToken != null) {
                this.goals = LocalStorage.getInstance().getGoals();
            } else if (this.goals != null) {
                this.goals.clear();
            }
        }
    }

    private void handleTransactionUpdate(Transaction transaction) {
        System.out.println("Processing transaction: " + transaction);
    
        // 1. Find the currently active goal
        SavingGoal activeGoal = null;
    
        for (SavingGoal goal : goals) {
            if (goal.isActive()) {
                activeGoal = goal;
                break;
            }
        }
    
        // 2. If no goal is active, exit early
        if (activeGoal == null) {
            System.out.println("No active saving goal to update.");
            return;
        }
    
        // 3. Extract the amount from the transaction
        float amount = transaction.getAmount(); 
    
        if (amount > 0) {
            float newAmount = activeGoal.getCurrentAmount() + amount;
            activeGoal.setCurrentAmount(newAmount);

            LocalStorage.getInstance().save(DataKey.GOALS, new ArrayList<>(goals));

            System.out.println("Added " + amount + " to goal: " + activeGoal.getName() + ". New balance: " + newAmount);
            
    
            if (activeGoal.getCurrentAmount() >= activeGoal.getTargetAmount()) {
                System.out.println("Congratulations! The saving goal '" + activeGoal.getName() + "' is fully funded!");
                
                support.firePropertyChange(GoalEvent.GOAL_COMPLETED.getName(), null, activeGoal);
            }
        }
    }

}