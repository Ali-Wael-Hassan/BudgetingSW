package com.duck.model.accountOps;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import com.duck.model.type.Account;
import com.duck.model.type.AccountConfig;
import com.duck.model.type.AppSettings;
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
        if ("token".equals(evt.getPropertyName())) {
            String newToken = (String) evt.getNewValue();
            if (newToken != null) {
                this.accounts = LocalStorage.getInstance().getAccounts();
            } else if (this.accounts != null) {
                this.accounts.clear();
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
        return LocalStorage.getInstance().save(AppSettings.DataKey.ACCOUNTS, accounts);
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