package com.duck.model.authentication;

import com.duck.model.type.*;
import com.duck.model.dataAccessors.StorageStrategy;

public interface Auth {
    String getPasswordStrength(String password);
    AppSettings.Message emailExists(String email);
    AppSettings.Message reachedRequestLimit();
    AppSettings.Message checkAgainstDataBase(Account account);
    AppSettings.Message setStorageStrategy(StorageStrategy strategy);
}