package com.duck.model.type;

import java.util.Objects;

/**
 * Represents a user account with authentication credentials, a monetary
 * balance, and an associated AccountConfig (theme, currency, avatar).
 * Equality is based on the email field.
 */
public class Account {
    private String email;
    private String userName;
    private String password;
    private float balance;
    private AccountConfig accountConfig;

    /** Constructs an empty Account. */
    public Account() {}

    /**
     * Constructs an Account with the given values.
     * @param email         the account email
     * @param userName      the display name
     * @param password      the account password
     * @param balance       the initial balance
     * @param accountConfig the configuration (theme, currency, avatar)
     */
    public Account(String email, String userName, String password, float balance, AccountConfig accountConfig) {
        // 1. initialize credintials
        this.email = email;
        this.userName = userName;
        this.password = password;
        // 2. set financial data
        this.balance = balance;
        // 3. apply configurations
        this.accountConfig = accountConfig;
    }

    /** @return the account email */
    public String getEmail() { return this.email; }

    /** @return the display name */
    public String getUserName() { return this.userName; }

    /** @return the account password */
    public String getPassword() { return this.password; }

    /** @return the current balance */
    public float getBalance() { return this.balance; }

    /** @return the account configuration */
    public AccountConfig getAccountConfig() { return this.accountConfig; }

    /** @param email the new email */
    public void setEmail(String email) { this.email = email; }

    /** @param password the new password */
    public void setPassword(String password) { this.password = password; }

    /** @param balance the new balance */
    public void setBalance(float balance) { this.balance = balance; }

    /** @param accountConfig the new configuration */
    public void setAccountConfig(AccountConfig accountConfig) { this.accountConfig = accountConfig; }

    /** @param userName the new display name */
    public void setUserName(String userName) { this.userName = userName; }

    @Override
    public boolean equals(Object o) {
        // 1. check refrence
        if (this == o) return true;
        // 2. check type    
        if (!(o instanceof Account)) return false;
        // 3. verify identity
        Account account = (Account) o;
        return Objects.equals(email, account.email);
    }

    @Override
    public int hashCode() {
        // generate hash    
        return Objects.hash(email);
    }
}
