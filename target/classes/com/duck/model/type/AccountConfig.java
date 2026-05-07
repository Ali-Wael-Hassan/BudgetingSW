package com.duck.model.type;

import java.awt.Image;

public class AccountConfig {

    private Image avatar;
    private String avatarPath;
    private AppSettings.Mode mode;
    private AppSettings.Currency curreny;

    public AccountConfig() {
        this.mode = AppSettings.Mode.DARK;
        this.curreny = AppSettings.Currency.USD;
    }

    public AccountConfig(String displayName, Image avatar, AppSettings.Mode mode, AppSettings.Currency curreny) {
        this.avatar = avatar;
        this.mode = mode;
        this.curreny = curreny;
    }

    public Image getAvatar() { return avatar; }

    public String getAvatarPath() { return avatarPath; }

    public AppSettings.Mode getMode() { return this.mode; }

    public AppSettings.Currency getCurrency() { return this.curreny; }

    public void setAvatar(Image avatar) { this.avatar = avatar; }

    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    public void setMode(AppSettings.Mode mode) { this.mode = mode; }

    public void setCurrency(AppSettings.Currency curreny) { this.curreny = curreny; }
}
