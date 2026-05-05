package com.duck.model.authentication;

import com.duck.model.type.*;

public abstract class Recognition {
    protected Auth authStrategy;
    protected Session currentSession = Session.getInstance();

    public AppSettings.Message perform(Account account) {
        if (validate(account) == AppSettings.Message.SUCCESS) {
            String generatedToken = "tok_" + account.getEmail() + "_" + System.currentTimeMillis();
            currentSession.saveToken(generatedToken);
            return redirection();
        }
        return AppSettings.Message.ERROR;
    }

    public abstract AppSettings.Message redirection();

    public void setAuthStrategy(Auth strategy) {
        this.authStrategy = strategy;
    }

    public String getActiveToken() {
        return currentSession.getToken();
    }

    protected abstract AppSettings.Message validate(Account account);
}
