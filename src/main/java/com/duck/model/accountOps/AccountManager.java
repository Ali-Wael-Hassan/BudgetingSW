package com.duck.model.accountOps;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import com.duck.model.type.Account;
import com.duck.model.type.AccountConfig;
import com.duck.model.type.AppSettings;
import com.duck.model.type.AppSettings.AccountEvent;
import com.duck.model.dataAccessors.LocalStorage;
import com.duck.model.authentication.Session;

public class AccountManager implements PropertyChangeListener {
    private List<Account> accounts;
    private Session session = Session.getInstance();

    public AccountManager() {
        if (session.getToken() != null) {
            this.accounts = LocalStorage.getInstance().getAccounts();
        }
        session.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (AccountEvent.TOKEN_CHANGED.getName().equals(evt.getPropertyName())) {
            String newToken = (String) evt.getNewValue();
            if (newToken != null) {
                this.accounts = LocalStorage.getInstance().getAccounts();
            } else {
                this.accounts = null;
            }
        }
    }

    public AppSettings.Message editAccount(Account acc, AccountConfig config) {
        // 1. Check if we even have accounts loaded
        if (accounts == null || accounts.isEmpty()) {
            return AppSettings.Message.ERROR;
        }

        // 2. Validate Token & Identity
        String decryptedToken = session.getToken();
        if (decryptedToken == null) return AppSettings.Message.ERROR;

        String emailFromToken = extractEmailFromToken(decryptedToken);
        if (emailFromToken == null || !emailFromToken.equals(acc.getEmail())) {
            return AppSettings.Message.ERROR;
        }

        // 3. Perform Update
        acc.setAccountConfig(config);
        
        // 4. Persist
        return saveAll();
    }

    private AppSettings.Message saveAll() {
        return LocalStorage.getInstance().save(AppSettings.DataKey.ACCOUNTS, new java.util.ArrayList<>(accounts));
    }

    public AppSettings.Message updatePassword(Account acc, String oldPassword, String newPassword) {
        if (accounts == null || accounts.isEmpty()) return AppSettings.Message.ERROR;
        String decryptedToken = session.getToken();
        if (decryptedToken == null) return AppSettings.Message.ERROR;
        String emailFromToken = extractEmailFromToken(decryptedToken);
        if (emailFromToken == null || !emailFromToken.equals(acc.getEmail())) return AppSettings.Message.ERROR;
        if (!acc.getPassword().equals(oldPassword)) return AppSettings.Message.ERROR;
        acc.setPassword(newPassword);
        return saveAll();
    }

    public AppSettings.Message deleteAccount(Account acc) {
        if (accounts == null || accounts.isEmpty()) return AppSettings.Message.ERROR;
        String decryptedToken = session.getToken();
        if (decryptedToken == null) return AppSettings.Message.ERROR;
        String emailFromToken = extractEmailFromToken(decryptedToken);
        if (emailFromToken == null || !emailFromToken.equals(acc.getEmail())) return AppSettings.Message.ERROR;

        LocalStorage storage = LocalStorage.getInstance();

        storage.getExpenses().removeIf(t -> acc.equals(t.getAccount()));
        storage.getIncome().removeIf(t -> acc.equals(t.getAccount()));
        storage.getBudgets().removeIf(b -> acc.equals(b.getAccount()));
        storage.getGoals().removeIf(g -> acc.equals(g.getAccount()));

        accounts.remove(acc);
        saveAll();
        storage.save(AppSettings.DataKey.EXPENSES, new java.util.ArrayList<>(storage.getExpenses()));
        storage.save(AppSettings.DataKey.INCOME, new java.util.ArrayList<>(storage.getIncome()));
        storage.save(AppSettings.DataKey.BUDGETS, new java.util.ArrayList<>(storage.getBudgets()));
        storage.save(AppSettings.DataKey.GOALS, new java.util.ArrayList<>(storage.getGoals()));

        session.saveToken(null);
        return AppSettings.Message.SUCCESS;
    }

    public AppSettings.Message updateAccountName(Account acc, String newName) {
        if (accounts == null || accounts.isEmpty()) return AppSettings.Message.ERROR;
        String decryptedToken = session.getToken();
        if (decryptedToken == null) return AppSettings.Message.ERROR;
        String emailFromToken = extractEmailFromToken(decryptedToken);
        if (emailFromToken == null || !emailFromToken.equals(acc.getEmail())) return AppSettings.Message.ERROR;
        acc.setUserName(newName);
        return saveAll();
    }

    private String extractEmailFromToken(String token) {
        try {
            String[] parts = token.split("_");
            return (parts.length >= 2) ? parts[1] : null;
        } catch (Exception e) {
            return null;
        }
    }
}