package com.duck.model.records;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

import com.duck.model.type.Transaction;
import com.duck.model.type.TransactionConfig;
import com.duck.model.type.AppSettings.Message;
import com.duck.model.type.AppSettings.TransactionEvent;

public class TransactionManager {

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
            
            support.firePropertyChange(TransactionEvent.TRANSACTION_RECEIVED.getName(), null, transaction);
            
            return Message.SUCCESS;
        }
        
        return check;
    }

    public List<Transaction> getTransactions(TransactionConfig config) {
        List<Transaction> filteredTransactions = new ArrayList<>();

        if (config == null) return transactions;

        for (Transaction t : transactions) {
            // Check if transaction matches the account in config
            boolean accountMatch = config.getAccount() == null || t.getAccount().equals(config.getAccount());
            
            // Check if transaction falls within date range
            boolean dateMatch = true; 
            if (config.getPeriod() != null) {
                dateMatch = config.getPeriod().contains(t.getDate());
            }

            if (accountMatch && dateMatch) {
                filteredTransactions.add(t);
            }
        }

        return filteredTransactions;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
}