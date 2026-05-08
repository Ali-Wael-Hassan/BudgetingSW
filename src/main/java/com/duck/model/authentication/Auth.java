package com.duck.model.authentication;

import com.duck.model.type.*;

/**
 * Interface for authentication operations.
 */
public interface Auth {

    /**
     * Evaluates the strength of a password.
     * @param password the password to evaluate
     * @return Weak, Medium, or Strong
     */
    String getPasswordStrength(String password);

    /**
     * Checks whether the given email already exists in storage.
     * @param email the email to look up
     * @return SUCCESS if found, NOT_FOUND otherwise, ERROR on failure
     */
    AppSettings.Message emailExists(String email);

    /**
     * Checks whether the request limit has been reached.
     * @return SUCCESS if requests are allowed, or an error code
     */
    AppSettings.Message reachedRequestLimit();

    /**
     * Verifies the given account credentials against stored data.
     * @param account the account with email and password to check
     * @return SUCCESS on match, NOT_FOUND if no matching account,
     *         INVALID_EMAIL if the format is bad, ERROR on failure
     */
    AppSettings.Message checkAgainstDataBase(Account account);
}