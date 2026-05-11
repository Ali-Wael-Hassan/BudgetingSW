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

/**
 * Handles authenticated account CRUD operations.  Listens for token
 * changes via Session and reloads the account list accordingly.
 */
public class AccountManager implements PropertyChangeListener {
    private List<Account> accounts;
    private Session session = Session.getInstance();

    /**
     * Loads accounts if a session token exists and registers as a
     * listener for token changes.
     */
    public AccountManager() {
        if (session.getToken() != null) {
            // 1. load data if session exist
            this.accounts = LocalStorage.getInstance().getAccounts();
        }
        // 2. listen for token changes
        session.addPropertyChangeListener(this);
    }

    /**
     * Reloads or clears the account list when the session token
     * changes.
     * @param evt the property change event
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // 1. check session token
        if (AccountEvent.TOKEN_CHANGED.getName().equals(evt.getPropertyName())) {
            String newToken = (String) evt.getNewValue();
            // 2. wipe or reload data
            if (newToken != null) {
                this.accounts = LocalStorage.getInstance().getAccounts();
            } else {
                this.accounts = null;
            }
        }
    }

    /**
     * Updates an account's configuration after validating the session
     * token matches the account email.
     * @param acc    the account to update
     * @param config the new AccountConfig to apply
     * @return SUCCESS or ERROR
     */
    public AppSettings.Message editAccount(Account acc, AccountConfig config) {
        // 1. verify session activity
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

        // 3. update & presist
        acc.setAccountConfig(config);
        return saveAll();
    }

    /**
     * Persists the current account list to LocalStorage.
     * @return SUCCESS or ERROR
     */
    private AppSettings.Message saveAll() {
        // presist account list to local storage
        return LocalStorage.getInstance().save(AppSettings.DataKey.ACCOUNTS, new java.util.ArrayList<>(accounts));
    }

    /**
     * Updates an account password after verifying the old password
     * and session token.
     * @param acc         the account to update
     * @param oldPassword the current password for verification
     * @param newPassword the new password to set
     * @return SUCCESS or ERROR
     */
    public AppSettings.Message updatePassword(Account acc, String oldPassword, String newPassword) {
        // 1. verify session activity
        if (accounts == null || accounts.isEmpty()) return AppSettings.Message.ERROR;
        String decryptedToken = session.getToken();
        if (decryptedToken == null) return AppSettings.Message.ERROR;
        // 2. validate ownership
        String emailFromToken = extractEmailFromToken(decryptedToken);
        if (emailFromToken == null || !emailFromToken.equals(acc.getEmail())) return AppSettings.Message.ERROR;
        // 3. check if old pass. matches
        if (!acc.getPassword().equals(oldPassword)) return AppSettings.Message.ERROR;
        // 4. update & presist
        acc.setPassword(newPassword);
        return saveAll();
    }

    /**
     * Deletes an account and all its associated data (transactions,
     * budgets, goals), then clears the session.
     * @param acc the account to delete
     * @return SUCCESS or ERROR
     */
    public AppSettings.Message deleteAccount(Account acc) {
        // 1. verify session activity
        if (accounts == null || accounts.isEmpty()) return AppSettings.Message.ERROR;
        String decryptedToken = session.getToken();
        if (decryptedToken == null) return AppSettings.Message.ERROR;
        // 2. ensure the current user owns the account
        String emailFromToken = extractEmailFromToken(decryptedToken);
        if (emailFromToken == null || !emailFromToken.equals(acc.getEmail())) return AppSettings.Message.ERROR;

        LocalStorage storage = LocalStorage.getInstance();
        
        // 3. wipe financial records
        storage.getExpenses().removeIf(t -> acc.equals(t.getAccount()));
        storage.getIncome().removeIf(t -> acc.equals(t.getAccount()));
        storage.getBudgets().removeIf(b -> acc.equals(b.getAccount()));
        storage.getGoals().removeIf(g -> acc.equals(g.getAccount()));

        // 4. remove account
        accounts.remove(acc);
        // 5. save all chamges
        saveAll();  
        storage.save(AppSettings.DataKey.EXPENSES, new java.util.ArrayList<>(storage.getExpenses()));
        storage.save(AppSettings.DataKey.INCOME, new java.util.ArrayList<>(storage.getIncome()));
        storage.save(AppSettings.DataKey.BUDGETS, new java.util.ArrayList<>(storage.getBudgets()));
        storage.save(AppSettings.DataKey.GOALS, new java.util.ArrayList<>(storage.getGoals()));

        // 6. clear session
        session.saveToken(null);
        return AppSettings.Message.SUCCESS;
    }

    /**
     * Updates the display name for an account after session
     * validation.
     * @param acc     the account to update
     * @param newName the new user name
     * @return SUCCESS or ERROR
     */
    public AppSettings.Message updateAccountName(Account acc, String newName) {
        // 1. verify session activity
        if (accounts == null || accounts.isEmpty()) return AppSettings.Message.ERROR;
        // 2. get session token
        String decryptedToken = session.getToken();
        if (decryptedToken == null) return AppSettings.Message.ERROR;
        // 3. ensure token match email
        String emailFromToken = extractEmailFromToken(decryptedToken);
        if (emailFromToken == null || !emailFromToken.equals(acc.getEmail())) return AppSettings.Message.ERROR;
        // 4. update & presist
        acc.setUserName(newName);
        return saveAll();
    }

    /**
     * Extracts the email portion from a session token.
     * Token format: tok_email_timestamp.
     * @param token the session token
     * @return the email string, or null if parsing fails
     */
    private String extractEmailFromToken(String token) {
        try {
            // 1. splits token with "_"
            String[] parts = token.split("_");
            // 2. return email
            return (parts.length >= 2) ? parts[1] : null;
        } catch (Exception e) {
            return null;
        }
    }
}