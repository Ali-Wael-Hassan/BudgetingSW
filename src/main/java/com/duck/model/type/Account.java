package com.duck.model.type;

public class Account {
    private Email email;
    private UserName userName;
    private String password;
    private float balance;
    private AccountConfig accountConfig;

    public Account(Email email, UserName userName, String password, float balance, AccountConfig accountConfig) {
        this.email = email;
        this.userName = userName;
        this.password = password;
        this.balance = balance;
        this.accountConfig = accountConfig;
    }

    public String getEmail() { return this.email.getEmail(); }

    public UserName getUserName() { return this.userName; }

    public String getPassword() { return this.password; }

    public float getBalance() { return this.balance; }

    public AccountConfig getAccountConfig() { return this.accountConfig; }

    public void setPassword(String password) { this.password = password; }

    public void setBalance(float balance) { this.balance = balance; }

    public void setAccountConfig(AccountConfig accountConfig) { this.accountConfig = accountConfig; }
}
