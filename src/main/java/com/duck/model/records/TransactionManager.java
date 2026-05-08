package com.duck.model.records;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

import com.duck.model.dataAccessors.LocalStorage;
import com.duck.model.type.Transaction;
import com.duck.model.type.TransactionConfig;
import com.duck.model.type.AppSettings.DataKey;
import com.duck.model.type.AppSettings.Message;
import com.duck.model.type.AppSettings.TransactionEvent;
import com.duck.model.type.AppSettings.TransactionType;
import com.duck.model.type.AppSettings.AccountEvent;

public class TransactionManager implements PropertyChangeListener {

    private List<Transaction> transactions = new ArrayList<>();
    private final PropertyChangeSupport support;
    
    public TransactionManager() {
        support = new PropertyChangeSupport(this);
    }

    public Message validateTransaction(Transaction transaction) {
        // 1. Basic null check
        if (transaction == null) {
            return Message.NULL_TRANSACTION_ERROR;
        }

        // 2. Amount validation (Transaction must have a value)
        if (transaction.getAmount() <= 0) {
            return Message.INVALID_TRANSACTION_AMOUNT;
        }

        // 3. Category validation
        if (transaction.getCategory() == null || transaction.getCategory().trim().isEmpty()) {
            return Message.INVALID_CATEGORY;
        }

        // 4. Date validation
        if (transaction.getDate() == null || transaction.getDate().isAfter(LocalDate.now())) {
            return Message.INVALID_DATE;
        }

        // 5. Account validation
        if (transaction.getAccount() == null) {
            return Message.NULL_ACCOUNT;
        }

        return Message.SUCCESS;
    }

    public Message addTransaction(Transaction transaction) {
        // Validate first!
        Message check = validateTransaction(transaction);
        
        if (check == Message.SUCCESS) {
            transactions.add(transaction);

            if(!LocalStorage.getInstance().getCategories().contains(transaction.getCategory()))
                LocalStorage.getInstance().insert(DataKey.CATEGORIES, transaction.getCategory());

            if (transaction.getType() == TransactionType.INCOME)
                LocalStorage.getInstance().insert(DataKey.INCOME, transaction);
            else
                LocalStorage.getInstance().insert(DataKey.EXPENSES, transaction);

            support.firePropertyChange(TransactionEvent.TRANSACTION_RECEIVED.getName(), null, transaction);
            
            return Message.SUCCESS;
        }
        
        return check;
    }

    public List<Transaction> getTransactions(TransactionConfig config) {
        List<Transaction> filteredTransactions = new ArrayList<>();

        if (config == null) return transactions;

        for (Transaction t : transactions) {
            try {
                boolean accountMatch = config.getAccount() == null || t.getAccount().equals(config.getAccount());
                
                boolean dateMatch = true; 
                if (config.getPeriod() != null) {
                    dateMatch = config.getPeriod().contains(t.getDate());
                }

                boolean typeMatch = true;
                if (config.getType() != null) {
                    typeMatch = t.getType() == config.getType();
                }

                if (accountMatch && dateMatch && typeMatch) {
                    filteredTransactions.add(t);
                }
            } catch (Exception ignored) {
            }
        }

        return filteredTransactions;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (AccountEvent.TOKEN_CHANGED.getName().equals(evt.getPropertyName())) {
            String newToken = (String) evt.getNewValue();
            if (newToken != null) {
                transactions.clear();
                transactions.addAll(LocalStorage.getInstance().getIncome());
                transactions.addAll(LocalStorage.getInstance().getExpenses());
            } else if (transactions != null) {
                transactions.clear();
            }
        }
    }
}