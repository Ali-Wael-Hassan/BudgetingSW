package com.duck.model.authentication;

import com.duck.model.dataAccessors.LocalStorage;
import com.duck.model.type.*;
import com.duck.model.type.AppSettings.DataKey;

public class SignUp extends Recognition {
    @Override
    protected AppSettings.Message validate(Account account) {
            System.out.println("--- Starting SignUp Validation ---");

            // 1. Check if the account object itself is valid
            if (account == null) {
                System.out.println("FAIL: Account object is null.");
                return AppSettings.Message.ERROR; 
            }

            // 2. Validate Email
            String emailStr = account.getEmail();
            System.out.println("Checking email: " + emailStr);
            if (emailStr == null || emailStr.trim().isEmpty() || !emailStr.contains("@")) {
                System.out.println("FAIL: Email is null, empty, or missing '@'.");
                return AppSettings.Message.ERROR; 
            }

            // 3. Validate Password
            String password = account.getPassword();
            System.out.println("Checking password (length & regex)...");
            if (password == null || password.length() < 8) {
                System.out.println("FAIL: Password length is less than 8.");
                return AppSettings.Message.ERROR;
            }
            // Must contain at least one letter and one number
            if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d).+$")) {
                System.out.println("FAIL: Password regex failed (needs letter + number).");
                return AppSettings.Message.ERROR;
            }

            // 4. Validate Starting Balance
            System.out.println("Checking balance: " + account.getBalance());
            if (account.getBalance() < 0.0f) {
                System.out.println("FAIL: Balance is negative.");
                return AppSettings.Message.ERROR;
            }

            // 5. Database Check for Sign Up
            System.out.println("Checking database if email already exists...");
            AppSettings.Message flag = authStrategy.emailExists(account.getEmail());
            System.out.println("Database check returned: " + flag);
            
            if (flag == AppSettings.Message.SUCCESS || flag == AppSettings.Message.INVALID_EMAIL) {
                System.out.println("FAIL: Email already exists or is rejected by the database.");
                return AppSettings.Message.ERROR;
            }

            System.out.println("SUCCESS: Validation passed! Inserting into database...");
            LocalStorage.getInstance().insert(DataKey.ACCOUNTS, account);   

            return AppSettings.Message.SUCCESS;
    }

    @Override
    public AppSettings.Message redirection() {
        return AppSettings.Message.SUCCESS;
    }
}