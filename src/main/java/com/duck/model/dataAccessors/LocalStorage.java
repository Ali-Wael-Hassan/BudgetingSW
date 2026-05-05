package com.duck.model.dataAccessors;

import com.duck.model.type.*;
import com.duck.model.type.AppSettings.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocalStorage implements StorageStrategy {
    private static LocalStorage instance;
    private final String FILE_PATH = "local_storage.json";
    private final ObjectMapper mapper;

    private List<Account> accounts = new ArrayList<>();
    private List<Transaction> expenses = new ArrayList<>();
    private List<Transaction> income = new ArrayList<>();
    private List<TransactionConfig> budgets = new ArrayList<>();
    private List<String> categories = new ArrayList<>();
    private List<SavingGoal> goals = new ArrayList<>();

    private LocalStorage() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        
        loadFromFile();

        if (categories.isEmpty()) {
            categories.add("Food");
            categories.add("Transport");
            categories.add("Salary");
        }
    }

    public static synchronized LocalStorage getInstance() {
        if (instance == null) {
            instance = new LocalStorage();
        }
        return instance;
    }

    private void saveToFile() throws IOException {
        mapper.writeValue(new File(FILE_PATH), this);
    }

    private void loadFromFile() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try {
                LocalStorage data = mapper.readValue(file, LocalStorage.class);
                this.accounts = data.accounts;
                this.expenses = data.expenses;
                this.income = data.income;
                this.budgets = data.budgets;
                this.categories = data.categories;
                this.goals = data.goals;
            } catch (IOException e) {
                System.err.println("Could not load data: " + e.getMessage());
            }
        }
    }

    @Override
    public Object fetch(AppSettings.DataKey key) {
        switch (key) {
            case ACCOUNTS:   return this.accounts;
            case EXPENSES:   return this.expenses;
            case INCOME:     return this.income;
            case BUDGETS:    return this.budgets;
            case CATEGORIES: return this.categories;
            case GOALS:      return this.goals;
            default:         return null;
        }
    }

    @Override
    public AppSettings.Message save(AppSettings.DataKey key, Object data) {
        try {
            if (data == null) return AppSettings.Message.ERROR;
            Object currentStorage = fetch(key);

            if (currentStorage instanceof List && data instanceof List) {
                List targetList = (List) currentStorage;
                targetList.clear();
                targetList.addAll((List) data);
                
                saveToFile();
                return AppSettings.Message.SUCCESS;
            }
            return AppSettings.Message.ERROR;
        } catch (Exception e) {
            return AppSettings.Message.ERROR;
        }
    }

    @Override
    public AppSettings.Message insert(AppSettings.DataKey key, Object data) {
        try {
            if (data == null) return AppSettings.Message.ERROR;
            Object storage = fetch(key);

            if (storage instanceof List) {
                ((List) storage).add(data);
                
                saveToFile();
                return AppSettings.Message.SUCCESS;
            }
            return AppSettings.Message.ERROR;
        } catch (Exception e) {
            return AppSettings.Message.ERROR;
        }
    }

    public List<Account> getAccounts() { return accounts; }
    public List<Transaction> getExpenses() { return expenses; }
    public List<Transaction> getIncome() { return income; }
    public List<TransactionConfig> getBudgets() { return budgets; }
    public List<String> getCategories() { return categories; }
    public List<SavingGoal> getGoals() { return goals; }
}