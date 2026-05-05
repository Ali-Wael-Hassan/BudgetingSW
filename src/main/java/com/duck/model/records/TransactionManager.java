package com.duck.model.records;

import java.util.ArrayList;
import java.util.List;

import com.duck.model.type.Transaction;
import com.duck.model.type.TransactionConfig;
import com.duck.model.type.AppSettings.Message;

public class TransactionManager {

    private List<IObserver> observers;

    public TransactionManager() {
        this.observers = new ArrayList<IObserver>();
    }

    public void addObserver(IObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(IObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(Transaction transaction) {
        for (IObserver observer : observers) {
            observer.update(transaction);
        }
    }

    public Message addTransaction(Transaction transaction) {

        // Add transaction logic here

        notifyObservers(transaction);
        return Message.SUCCESS;
    }

    public String alert(Budget budget) {

        //what to alert about? maybe if a transaction exceeds the budget?
        return "Budget Alert: Evaluation logic goes here.";
    }

    public List<Transaction> getTransactions(TransactionConfig config) {
        List<Transaction> transactions = new ArrayList<>();

        // Logic to retrieve transactions based on the provided config

        return transactions;
    }
}
