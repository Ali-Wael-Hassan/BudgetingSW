package com.duck.model.authentication;

import com.duck.model.type.*;

/**
 * Handles the login authentication flow.  Validates credentials via
 * the auth strategy and redirects on success.
 */
public class Login extends Recognition {
    /**
     * Validates the account by delegating to the auth strategy's
     * database check.
     * @param account the account to validate
     * @return SUCCESS if credentials match, ERROR otherwise
     */
    @Override
    protected AppSettings.Message validate(Account account) {
        // 1. delegate validation
        AppSettings.Message flag = authStrategy.checkAgainstDataBase(account);
        // 2. check
        if(flag == AppSettings.Message.SUCCESS) {
            return AppSettings.Message.SUCCESS;
        }

        // 3. error handler
        return AppSettings.Message.ERROR;
    }

    /**
     * Returns the post-login navigation target.
     * @return SUCCESS
     */
    @Override
    public AppSettings.Message redirection() {
        // return target
        return AppSettings.Message.SUCCESS;
    }
}