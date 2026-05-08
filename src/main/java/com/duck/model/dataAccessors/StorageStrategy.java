package com.duck.model.dataAccessors;

import com.duck.model.type.AppSettings;

/**
 * Strategy interface for data persistence operations.
 */
public interface StorageStrategy {

    /**
     * Retrieves data associated with the given key.
     * @param key the data key to look up
     * @return the stored object, or null if not found
     */
    Object fetch(AppSettings.DataKey key);

    /**
     * Replaces all data for the given key with the provided data.
     * @param key  the data key to overwrite
     * @param data the new data to store
     * @return SUCCESS or ERROR
     */
    AppSettings.Message save(AppSettings.DataKey key, Object data);

    /**
     * Appends a single item to the collection for the given key.
     * @param key  the data key to append to
     * @param data the item to add
     * @return SUCCESS or ERROR
     */
    AppSettings.Message insert(AppSettings.DataKey key, Object data);
}