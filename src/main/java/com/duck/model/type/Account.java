package com.duck.model.type;

import java.util.Objects;

public class Account {
    private String email;
    private String userName;
    private String password;
    private float balance;
    private AccountConfig accountConfig;

    public Account() {}

    public Account(String email, String userName, String password, float balance, AccountConfig accountConfig) {
        this.email = email;
        this.userName = userName;
        this.password = password;
        this.balance = balance;
        this.accountConfig = accountConfig;
    }

    public String getEmail() { return this.email; }

    public String getUserName() { return this.userName; }

    public String getPassword() { return this.password; }

    public float getBalance() { return this.balance; }

    public AccountConfig getAccountConfig() { return this.accountConfig; }

    public void setEmail(String email) { this.email = email; }

    public void setPassword(String password) { this.password = password; }

    public void setBalance(float balance) { this.balance = balance; }

    public void setAccountConfig(AccountConfig accountConfig) { this.accountConfig = accountConfig; }

    public void setUserName(String userName) { this.userName = userName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account = (Account) o;
        return Objects.equals(email, account.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
