package com.duck;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    // ── FXML injections ────────────────────────────────────────────
    @FXML
    private Label profileNameLabel;
    @FXML
    private Label profileEmailLabel;

    @FXML
    private HBox themeToggleBox;

    @FXML
    private HBox currencyRow1;
    @FXML
    private HBox currencyRow2;

    @FXML
    private VBox notificationOptions;
    @FXML
    private VBox securityOptions;

    // ── User model ─────────────────────────────────────────────────
    public static class UserProfile {
        public String name;
        public String email;
        public String selectedTheme; // "Dark" | "Light" | "System"
        public String selectedCurrency; // "USD" | "EUR" etc.

        public UserProfile(String name, String email,
                String selectedTheme, String selectedCurrency) {
            this.name = name;
            this.email = email;
            this.selectedTheme = selectedTheme;
            this.selectedCurrency = selectedCurrency;
        }
    }

    // ── Notification toggle model ──────────────────────────────────
    public static class NotificationSetting {
        public String label;
        public String description;
        public boolean enabled;

        public NotificationSetting(String label, String description, boolean enabled) {
            this.label = label;
            this.description = description;
            this.enabled = enabled;
        }
    }

    // ── Security action model ──────────────────────────────────────
    public static class SecurityAction {
        public String label;
        public String description;
        public String buttonText;
        public String buttonStyle; // CSS style class for the button

        public SecurityAction(String label, String description,
                String buttonText, String buttonStyle) {
            this.label = label;
            this.description = description;
            this.buttonText = buttonText;
            this.buttonStyle = buttonStyle;
        }
    }

    // ── Data ───────────────────────────────────────────────────────
    private UserProfile user;

    // Theme options (label, isDefault)
    private static final String[] THEMES = { "Dark", "Light", "System" };

    /*
     * Currency options (display label, currency code, isDefault)
     *
     * FIX: Non-ASCII symbols are stored as Java Unicode escapes here,
     * NOT in FXML, to avoid XML encoding issues:
     * € = \u20AC
     * 
     * £ = \u00A3
     * ¥ = \u00A5
     * ر.س = Arabic (valid UTF-8; kept as literal in Java source)
     * ج.م = Arabic (valid UTF-8; kept as literal in Java source)
     */
    private static final String[][] CURRENCIES_ROW1 = {
            { "$ USD", "USD", "true" },
            { "\u20AC EUR", "EUR", "false" }, // € EUR
            { "\u00A3 GBP", "GBP", "false" }, // £ GBP
    };
    private static final String[][] CURRENCIES_ROW2 = {
            { "\u00A5 JPY", "JPY", "false" }, // ¥ JPY
            { "\u0631.\u0633 SAR", "SAR", "false" }, // ر.س SAR
            { "\u062C.\u0645 EGP", "EGP", "false" }, // ج.م EGP
    };

    // ── Lifecycle ──────────────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        user = new UserProfile(
                "John Doe",
                "john.doe@example.com",
                "Dark",
                "USD");

        renderProfile();
        renderThemeToggles();
        renderCurrencyToggles();
        renderNotifications();
        renderSecurityActions();
    }

    // ── FXML handlers ──────────────────────────────────────────────

    @FXML
    private void handleEditProfile() {
        // TODO: open edit dialog and update user object, then call renderProfile()
        System.out.println("Edit Profile clicked");
    }

    @FXML
    private void handleAddTransaction() {
        // TODO: navigate to / open add-transaction dialog
        System.out.println("Add Transaction clicked");
    }

    @FXML
    private void handleDeleteAccount() {
        // TODO: show confirmation dialog before deleting
        System.out.println("Delete Account clicked");
    }

    // ── Profile Card ───────────────────────────────────────────────

    private void renderProfile() {
        profileNameLabel.setText(user.name);
        profileEmailLabel.setText(user.email);
    }

    // ── Card 1: Theme toggles ──────────────────────────────────────

    private void renderThemeToggles() {
        themeToggleBox.getChildren().clear();
        ToggleGroup group = new ToggleGroup();

        for (String label : THEMES) {
            ToggleButton btn = new ToggleButton(label);

            btn.getStyleClass().add("theme-toggle");
            btn.setToggleGroup(group);
            btn.setSelected(label.equals(user.selectedTheme));

            btn.setOnAction(e -> {
                user.selectedTheme = label;
                System.out.println("Theme changed to: " + label);
            });

            themeToggleBox.getChildren().add(btn);
        }
    }

    // ── Card 2: Currency toggles ───────────────────────────────────

    private void renderCurrencyToggles() {
        currencyRow1.getChildren().clear();
        currencyRow2.getChildren().clear();
        ToggleGroup group = new ToggleGroup();

        buildCurrencyRow(currencyRow1, CURRENCIES_ROW1, group);
        buildCurrencyRow(currencyRow2, CURRENCIES_ROW2, group);
    }

    private void buildCurrencyRow(HBox row, String[][] currencies, ToggleGroup group) {
        for (String[] currency : currencies) {
            String displayLabel = currency[0];
            String code = currency[1];

            ToggleButton btn = new ToggleButton(displayLabel);
            btn.getStyleClass().add("currency-toggle");
            btn.setToggleGroup(group);
            btn.setSelected(code.equals(user.selectedCurrency));

            btn.setOnAction(e -> {
                user.selectedCurrency = code;
                System.out.println("Currency changed to: " + code);
            });

            row.getChildren().add(btn);
        }
    }

    // ── Card 3: Notification toggles ──────────────────────────────

    private void renderNotifications() {
        notificationOptions.getChildren().clear();

        List<NotificationSetting> settings = List.of(
                new NotificationSetting("Push Notifications", "Receive notifications on your device", true),
                new NotificationSetting("Email Alerts", "Get important updates via email", true),
                new NotificationSetting("Budget Alerts", "Alert when approaching budget limits", true));

        for (NotificationSetting s : settings) {
            notificationOptions.getChildren().add(buildNotificationRow(s));
        }
    }

    private HBox buildNotificationRow(NotificationSetting s) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox();
        HBox.setHgrow(info, Priority.ALWAYS);
        Label title = new Label(s.label);
        title.getStyleClass().add("notification-label");
        Label desc = new Label(s.description);
        desc.getStyleClass().add("notification-desc");
        info.getChildren().addAll(title, desc);

        ToggleButton toggle = new ToggleButton();
        toggle.getStyleClass().add("toggle-switch");
        toggle.setSelected(s.enabled);
        toggle.setOnAction(e -> {
            s.enabled = toggle.isSelected();
            System.out.println(s.label + " -> " + s.enabled);
        });

        row.getChildren().addAll(info, toggle);
        return row;
    }

    // ── Card 4: Security actions ───────────────────────────────────

    private void renderSecurityActions() {
        securityOptions.getChildren().clear();

        List<SecurityAction> actions = List.of(
                new SecurityAction("Change Password", "Update your account password", "Change", "security-btn"),
                new SecurityAction("Two-Factor Authentication", "Add an extra layer of security", "Enable",
                        "security-btn-active"),
                new SecurityAction("Sign Out", "Sign out from all devices", "Sign Out", "signout-btn"));

        for (SecurityAction a : actions) {
            securityOptions.getChildren().add(buildSecurityRow(a));
        }
    }

    private HBox buildSecurityRow(SecurityAction a) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("security-item");

        VBox info = new VBox();
        HBox.setHgrow(info, Priority.ALWAYS);
        Label title = new Label(a.label);
        title.getStyleClass().add("security-label");
        Label desc = new Label(a.description);
        desc.getStyleClass().add("security-desc");
        info.getChildren().addAll(title, desc);

        Button btn = new Button(a.buttonText);
        btn.getStyleClass().add(a.buttonStyle);
        btn.setOnAction(e -> System.out.println(a.label + " button clicked"));

        row.getChildren().addAll(info, btn);
        return row;
    }
}
