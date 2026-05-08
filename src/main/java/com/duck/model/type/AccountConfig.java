package com.duck.model.type;

public class AccountConfig {

    private String avatarPath;
    private AppSettings.Mode mode;
    private AppSettings.Currency curreny;

    public AccountConfig() {
        this.mode = AppSettings.Mode.DARK;
        this.curreny = AppSettings.Currency.USD;
    }

    public AccountConfig(String avatarPath, AppSettings.Mode mode, AppSettings.Currency curreny) {
        this.avatarPath = avatarPath;
        this.mode = mode;
        this.curreny = curreny;
    }

    public String getAvatarPath() { return avatarPath; }

    public AppSettings.Mode getMode() { return this.mode; }

    public AppSettings.Currency getCurrency() { return this.curreny; }

    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    public void setMode(AppSettings.Mode mode) { this.mode = mode; }

    public void setCurrency(AppSettings.Currency curreny) { this.curreny = curreny; }
}
