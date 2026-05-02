package com.duck.model.authentication;

import com.duck.model.type.*;

public class SignUp extends Recognition {
    @Override
    protected AppSettings.Message validate(Account account) {
        if (authStrategy.emailExists(account.getEmail()) == AppSettings.Message.SUCCESS) {
            return AppSettings.Message.ERROR;
        }
        return AppSettings.Message.SUCCESS;
    }

    @Override
    public AppSettings.Message redirection() {
        return AppSettings.Message.SUCCESS;
    }
}