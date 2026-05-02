package com.duck.model.type;

import java.awt.Image;

public class AccountConfig {

    private String displayName;
    private Image avatar;
    private AppSettings.Mode mode;
    private AppSettings.Currency curreny;

    public AccountConfig(String displayName, Image avatar, AppSettings.Mode mode, AppSettings.Currency curreny) {
        this.displayName = displayName;
        this.avatar = avatar;
        this.mode = mode;
        this.curreny = curreny;
    }

    public String getDisplayName() { return this.displayName; }

    public Image getAvatar() { return avatar; }
    
    public AppSettings.Mode getMode() { return this.mode; }

    public AppSettings.Currency getCurrency() { return this.curreny; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public void setAvatar(Image avatar) { this.avatar = avatar; }

    public void setMode(AppSettings.Mode mode) { this.mode = mode; }

    public void setCurrency(AppSettings.Currency curreny) { this.curreny = curreny; }
}
