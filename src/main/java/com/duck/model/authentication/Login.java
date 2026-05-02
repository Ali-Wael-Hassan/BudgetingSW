package com.duck.model.authentication;

import com.duck.model.type.*;

public class Login extends Recognition {
    @Override
    protected AppSettings.Message validate(Account account) {
        AppSettings.Message flag = authStrategy.checkAgainstDataBase(account);
        if(flag == AppSettings.Message.SUCCESS) {
            return AppSettings.Message.SUCCESS;
        }

        return AppSettings.Message.ERROR;
    }

    @Override
    public AppSettings.Message redirection() {
        return AppSettings.Message.SUCCESS;
    }
}