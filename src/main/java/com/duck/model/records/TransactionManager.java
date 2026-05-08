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

/**
 * Manages all Transaction objects for the application.  Provides
 * validation, creation, filtered queries, and persistence.  Fires
 * TRANSACTION_RECEIVED events so that BudgetManager and
 * SavingGoalsManager can react to new transactions.
 */
public class TransactionManager implements PropertyChangeListener {

    private List<Transaction> transactions = new ArrayList<>();
    private final PropertyChangeSupport support;
    
    /** Constructs an empty TransactionManager with a PropertyChangeSupport. */
    public TransactionManager() {
        support = new PropertyChangeSupport(this);
    }

    /**
     * Validates a transaction against business rules.
     * @param transaction the transaction to validate
     * @return SUCCESS or the appropriate error Message
     */
    public Message validateTransaction(Transaction transaction) {
        if (transaction == null) {
            return Message.NULL_TRANSACTION_ERROR;
        }
        if (transaction.getAmount() <= 0) {
            return Message.INVALID_TRANSACTION_AMOUNT;
        }
        if (transaction.getCategory() == null || transaction.getCategory().trim().isEmpty()) {
            return Message.INVALID_CATEGORY;
        }
        if (transaction.getDate() == null || transaction.getDate().isAfter(LocalDate.now())) {
            return Message.INVALID_DATE;
        }
        if (transaction.getAccount() == null) {
            return Message.NULL_ACCOUNT;
        }
        return Message.SUCCESS;
    }

    /**
     * Adds a new transaction after validation.  Persists to storage and
     * fires a TRANSACTION_RECEIVED event on success.
     * @param transaction the transaction to add
     * @return SUCCESS or the validation error Message
     */
    public Message addTransaction(Transaction transaction) {
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

    /**
     * Returns transactions filtered by the given config.
     * Supports filtering by account, date period, and transaction type.
     * @param config the filter configuration, or null for all transactions
     * @return the filtered list of transactions
     */
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

    /** @param listener the listener to add */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Handles property change events.  Responds to TOKEN_CHANGED by
     * loading or clearing the persisted transaction list.
     * @param evt the property change event
     */
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