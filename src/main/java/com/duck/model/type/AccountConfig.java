package com.duck.model.type;

/**
 * Stores user preference settings for an Account: avatar image path,
 * color theme mode (DARK / LIGHT), and currency (USD / EUR / EGP).
 */
public class AccountConfig {

    private String avatarPath;
    private AppSettings.Mode mode;
    private AppSettings.Currency curreny;

    /** Constructs an AccountConfig with default DARK mode and USD currency. */
    public AccountConfig() {
        this.mode = AppSettings.Mode.DARK;
        this.curreny = AppSettings.Currency.USD;
    }

    /**
     * Constructs an AccountConfig with the given values.
     * @param avatarPath path to the avatar image file
     * @param mode       the display theme mode
     * @param curreny    the preferred currency
     */
    public AccountConfig(String avatarPath, AppSettings.Mode mode, AppSettings.Currency curreny) {
        this.avatarPath = avatarPath;
        this.mode = mode;
        this.curreny = curreny;
    }

    /** @return the avatar image file path */
    public String getAvatarPath() { return avatarPath; }

    /** @return the display theme mode */
    public AppSettings.Mode getMode() { return this.mode; }

    /** @return the preferred currency */
    public AppSettings.Currency getCurrency() { return this.curreny; }

    /** @param avatarPath the new avatar image path */
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    /** @param mode the new display theme mode */
    public void setMode(AppSettings.Mode mode) { this.mode = mode; }

    /** @param curreny the new preferred currency */
    public void setCurrency(AppSettings.Currency curreny) { this.curreny = curreny; }
}
