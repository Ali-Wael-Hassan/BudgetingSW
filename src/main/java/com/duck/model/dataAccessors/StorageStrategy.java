package com.duck.model.dataAccessors;

import com.duck.model.type.AppSettings;

public interface StorageStrategy {
    Object fetch(AppSettings.DataKey key);

    AppSettings.Message save(AppSettings.DataKey key, Object data);

    AppSettings.Message insert(AppSettings.DataKey key, Object data);
}