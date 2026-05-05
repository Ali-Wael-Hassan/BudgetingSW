package com.duck.model.accountOps;

import java.util.List;
import com.duck.model.type.Account;
import com.duck.model.type.AccountConfig;
import com.duck.model.type.AppSettings;
import com.duck.model.dataAccessors.LocalStorage;
import com.duck.model.authentication.Session;

public class AccountManager {
    private List<Account> accounts = LocalStorage.getInstance().getAccounts();;
    private Session session = Session.getInstance();

    public AccountManager() {}

    public AppSettings.Message editAccount(Account acc, AccountConfig config) {
        // Retrieve and Decrypt the token
        String decryptedToken = session.getToken();

        if (decryptedToken == null) {
            return AppSettings.Message.ERROR;
        }

        // Extract the email from the token
        String emailFromToken = extractEmailFromToken(decryptedToken);

        // Is the account being edited the same as the logged-in user
        if (emailFromToken == null || !emailFromToken.equals(acc.getEmail())) {
            return AppSettings.Message.ERROR;
        }

        acc.setAccountConfig(config);
        
        LocalStorage.getInstance().save(AppSettings.DataKey.ACCOUNTS, accounts);

        return AppSettings.Message.SUCCESS;
    }

    private String extractEmailFromToken(String token) {
        try {
            String[] parts = token.split("_");
            if (parts.length >= 2) {
                return parts[1]; 
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}