package com.duck.model.dataAccessors;

import com.duck.model.records.Budget;
import com.duck.model.type.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton JSON-file storage implementation.  Loads from and saves
 * to local_storage.json on every mutation.
 */
public class LocalStorage implements StorageStrategy {
    private static LocalStorage instance;
    private final String FILE_PATH = "local_storage.json";

    @JsonIgnore
    private final ObjectMapper mapper;

    private List<Account> accounts = new ArrayList<>();
    private List<Transaction> expenses = new ArrayList<>();
    private List<Transaction> income = new ArrayList<>();
    private List<Budget> budgets = new ArrayList<>();
    private List<String> categories = new ArrayList<>();
    private List<SavingGoal> goals = new ArrayList<>();

    /**
     * Private constructor.  Configures the ObjectMapper with JSR-310
     * support and lenient deserialization, then loads existing data
     * from the JSON file.  Seeds default categories if none exist.
     */
    private LocalStorage() {
        // 1. configure mapper
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // 2. load the data
        loadFromFile();

        // 3. seed defaults
        if (categories.isEmpty()) {
            categories.add("Food");
            categories.add("Transport");
            categories.add("Salary");
        }
    }

    /**
     * Returns the singleton LocalStorage instance, creating it if
     * necessary.
     * @return the singleton instance
     */
    public static synchronized LocalStorage getInstance() {
        // check for singleton
        if (instance == null) {
            instance = new LocalStorage();
        }
        return instance;
    }

    // =========================================================================
    // File I/O
    // =========================================================================

    /**
     * Serialises this object to the JSON file.
     */
    private void saveToFile() throws IOException {
        // write to disk
        mapper.writeValue(new File(FILE_PATH), this);
    }

    /**
     * Deserialises persisted data from the JSON file into the
     * in-memory lists.  Silently returns if the file does not exist.
     */
    private void loadFromFile() {
        // 1. check file
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try {
                // 2. map data
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

    // =========================================================================
    // StorageStrategy Implementation
    // =========================================================================

    /**
     * Returns the in-memory list for the given DataKey.
     * @param key the data key to look up
     * @return the matching list, or null if the key is unknown
     */
    @Override
    public Object fetch(AppSettings.DataKey key) {
        // target identification
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

    /**
     * Replaces the entire in-memory list for the given key with a new
     * list and persists to file.
     * @param key  the data key to overwrite
     * @param data the new List to store
     * @return SUCCESS or ERROR
     */
    @Override
    public AppSettings.Message save(AppSettings.DataKey key, Object data) {
        try {
            // 1. validate input
            if (data == null) return AppSettings.Message.ERROR;
            Object currentStorage = fetch(key);

            if (currentStorage instanceof List && data instanceof List) {
                // 2. clear and replace
                @SuppressWarnings("unchecked")
                List<Object> targetList = (List<Object>) currentStorage;
                targetList.clear();

                if (data instanceof List) {
                    List<?> dataList = (List<?>) data;
                    targetList.addAll((List<? extends Object>) dataList);
                }
                
                // 3. presist
                saveToFile();
                return AppSettings.Message.SUCCESS;
            }
            return AppSettings.Message.ERROR;
        } catch (Exception e) {
            return AppSettings.Message.ERROR;
        }
    }

    /**
     * Appends a single item to the in-memory list for the given key
     * and persists to file.
     * @param key  the data key to append to
     * @param data the item to add
     * @return SUCCESS or ERROR
     */
    @Override
    public AppSettings.Message insert(AppSettings.DataKey key, Object data) {
        try {
            // 1. check data
            if (data == null) return AppSettings.Message.ERROR;
            Object storage = fetch(key);

            if (storage instanceof List) {
                // 2. update
                @SuppressWarnings("unchecked")
                List<Object> typedStorage = (List<Object>) storage;
                typedStorage.add(data);
                
                // 3. presist
                saveToFile();
                return AppSettings.Message.SUCCESS;
            }
            return AppSettings.Message.ERROR;
        } catch (Exception e) {
            e.printStackTrace();
            return AppSettings.Message.ERROR;
        }
    }

    // =========================================================================
    // Getters
    // =========================================================================

    public List<Account> getAccounts() { return accounts; }
    public List<Transaction> getExpenses() { return expenses; }
    public List<Transaction> getIncome() { return income; }
    public List<Budget> getBudgets() { return budgets; }
    public List<String> getCategories() { return categories; }
    public List<SavingGoal> getGoals() { return goals; }
}