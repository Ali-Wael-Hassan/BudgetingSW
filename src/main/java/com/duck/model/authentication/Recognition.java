package com.duck.model.authentication;

import com.duck.model.type.*;

/**
 * Abstract template for authentication workflows.  The perform()
 * method runs validation, generates a token on success, and then
 * delegates to redirection().
 */
public abstract class Recognition {
    protected Auth authStrategy;
    protected Session currentSession = Session.getInstance();

    /**
     * Validates the account, generates a token on success, and
     * returns the redirection result.
     * @param account the account to authenticate
     * @return SUCCESS or ERROR
     */
    public AppSettings.Message perform(Account account) {
        // 1. run validation
        if (validate(account) == AppSettings.Message.SUCCESS) {
            // 2. generate session
            String generatedToken = "tok_" + account.getEmail() + "_" + System.currentTimeMillis();
            // 3. save state
            currentSession.saveToken(generatedToken);
            // 4. redirect
            return redirection();
        }
        // 5. error handler
        return AppSettings.Message.ERROR;
    }

    /**
     * Returns the navigation target after successful authentication.
     * @return SUCCESS or another redirect indicator
     */
    public abstract AppSettings.Message redirection();

    /**
     * Sets the authentication strategy to use.
     * @param strategy the Auth implementation
     */
    public void setAuthStrategy(Auth strategy) {
        // update strat.
        this.authStrategy = strategy;
    }

    /**
     * Returns the currently active session token.
     * @return the token string, or null if none exists
     */
    public String getActiveToken() {
        // fetch token
        return currentSession.getToken();
    }

    /**
     * Validates the given account according to subclass rules.
     * @param account the account to validate
     * @return SUCCESS or an error Message
     */
    protected abstract AppSettings.Message validate(Account account);
}
