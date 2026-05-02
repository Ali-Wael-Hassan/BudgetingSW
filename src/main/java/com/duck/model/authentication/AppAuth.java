package com.duck.model.authentication;

import com.duck.model.type.*;
import com.duck.model.dataAccessors.StorageStrategy;
import com.duck.model.dataAccessors.LocalStorage;
import java.util.List;

public class AppAuth implements Auth {
    private StorageStrategy storage;

    public AppAuth() {
        this.storage = LocalStorage.getInstance();
    }

    @Override
    public AppSettings.Message setStorageStrategy(StorageStrategy strategy) {
        if (strategy != null) {
            this.storage = strategy;
            return AppSettings.Message.SUCCESS;
        }
        return AppSettings.Message.ERROR;
    }

    @Override
    public AppSettings.Message emailExists(String email) {
        try {
            List<Account> accounts = (List<Account>) storage.fetch(AppSettings.DataKey.ACCOUNTS);
            
            if (accounts != null) {
                for (Account acc : accounts) {
                    if (acc.getEmail().equalsIgnoreCase(email)) {
                        return AppSettings.Message.SUCCESS;
                    }
                }
            }
            return AppSettings.Message.NOT_FOUND;
        } catch (Exception e) {
            return AppSettings.Message.ERROR;
        }
    }

    @Override
    public AppSettings.Message checkAgainstDataBase(Account account) {
        try {
            List<Account> accounts = (List<Account>) storage.fetch(AppSettings.DataKey.ACCOUNTS);
            
            if (accounts != null) {
                for (Account acc : accounts) {
                    if (acc.getEmail().equalsIgnoreCase(account.getEmail()) && 
                        acc.getPassword().equals(account.getPassword())) {
                        return AppSettings.Message.SUCCESS;
                    }
                }
            }
            return AppSettings.Message.ERROR;
        } catch (Exception e) {
            return AppSettings.Message.ERROR;
        }
    }

    @Override
    public String getPasswordStrength(String password) {
        if (password == null || password.length() < 6) return "Weak";
        if (password.matches(".*[0-9].*") && password.matches(".*[A-Z].*")) return "Strong";
        return "Medium";
    }

    @Override
    public AppSettings.Message reachedRequestLimit() {
        // Dummy Implementation
        return AppSettings.Message.SUCCESS;
    }
}