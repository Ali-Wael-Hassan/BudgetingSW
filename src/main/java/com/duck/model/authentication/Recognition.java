package com.duck.model.authentication;

import com.duck.model.type.*;

public abstract class Recognition {
    protected Auth authStrategy;

    public AppSettings.Message perform(Account account) {
        if (validate(account) == AppSettings.Message.SUCCESS) {
            return redirection();
        }
        return AppSettings.Message.ERROR;
    }

    public abstract AppSettings.Message redirection();

    public void setAuthStrategy(Auth strategy) {
        this.authStrategy = strategy;
    }

    protected abstract AppSettings.Message validate(Account account);
}
