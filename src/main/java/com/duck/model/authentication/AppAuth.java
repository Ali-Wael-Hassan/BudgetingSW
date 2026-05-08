package com.duck.model.authentication;

import com.duck.model.type.*;
import com.duck.model.type.AppSettings.Message;
import com.duck.model.dataAccessors.LocalStorage;
import java.util.List;

/**
 * Default authentication strategy backed by LocalStorage.
 */
public class AppAuth implements Auth {

    public AppAuth() {}

    // =========================================================================
    // Auth Implementation
    // =========================================================================

    /**
     * Checks whether the given email is already registered.
     * @param email the email to look up
     * @return SUCCESS if found, NOT_FOUND otherwise, ERROR on failure
     */
    @Override
    public AppSettings.Message emailExists(String email) {
        
        try {
            List<Account> accounts = LocalStorage.getInstance().getAccounts();
            
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

    /**
     * Verifies the account's email format and matches the credentials
     * against stored accounts.
     * @param account the account with email and password to check
     * @return SUCCESS on match, INVALID_EMAIL if format is bad,
     *         NOT_FOUND if no match, ERROR on failure
     */
    @Override
public AppSettings.Message checkAgainstDataBase(Account account) {
    System.out.println("--- DB Check: Starting ---");
    try {
        List<Account> accounts = LocalStorage.getInstance().getAccounts();
        System.out.println("DB Check: Loaded " + (accounts != null ? accounts.size() : "0") + " accounts from storage.");
        
        if (accounts != null) {
            String email = account.getEmail();
            System.out.println("DB Check: Validating format for: " + email);

            String[] atSplit = email.split("@");
            boolean checkAt = atSplit.length == 2;
            boolean checkDot = true;

            if (checkAt) {
                String[] dotSplit = atSplit[1].split("\\."); 
                checkDot = dotSplit.length >= 2; 
                System.out.println("DB Check: Format Parts - AtSplit: " + atSplit.length + ", DotSplit: " + dotSplit.length);
            }

            if (!checkAt || !checkDot) {
                System.out.println("DB Check: FAIL - Invalid email format detected.");
                return Message.INVALID_EMAIL;
            }
            
            System.out.println("DB Check: Searching for matching credentials...");
            for (Account acc : accounts) {
                if (acc.getEmail().equalsIgnoreCase(email)) {
                    System.out.println("DB Check: Email found. Checking password...");
                    if (acc.getPassword().equals(account.getPassword())) {
                        System.out.println("DB Check: SUCCESS - Credentials match.");
                        return AppSettings.Message.SUCCESS;
                    } else {
                        System.out.println("DB Check: Password mismatch for email: " + email);
                    }
                }
            }
        }
        
        System.out.println("DB Check: NOT_FOUND - No account matched.");
        return AppSettings.Message.NOT_FOUND;
        
    } catch (Exception e) {
        System.err.println("DB Check: CRITICAL ERROR during execution:");
        e.printStackTrace();
        return AppSettings.Message.ERROR;
    }
}

    /**
     * Evaluates password strength based on length and character rules.
     * @param password the password to evaluate
     * @return Weak if under 6 chars, Strong if it has digits and
     *         uppercase, Medium otherwise
     */
    @Override
    public String getPasswordStrength(String password) {
        if (password == null || password.length() < 6) return "Weak";
        if (password.matches(".*[0-9].*") && password.matches(".*[A-Z].*")) return "Strong";
        return "Medium";
    }

    /**
     * Placeholder for request-limit enforcement.
     * @return SUCCESS always
     */
    @Override
    public AppSettings.Message reachedRequestLimit() {
        // Dummy Implementation
        return AppSettings.Message.SUCCESS;
    }
}