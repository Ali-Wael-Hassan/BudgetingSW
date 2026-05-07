package com.duck;

import com.duck.model.accountOps.AccountManager;
import com.duck.model.type.Account;
import com.duck.model.type.AccountConfig;
import com.duck.model.type.AppSettings;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ProfileController implements Initializable, PropertyChangeListener {

    @FXML private StackPane rootPane;
    @FXML private StackPane sidebarAvatarContainer;
    @FXML private Label profileNameLabel;
    @FXML private Label profileEmailLabel;
    @FXML private StackPane profileAvatarContainer;
    @FXML private HBox themeToggleBox;
    @FXML private HBox currencyRow1;
    @FXML private HBox currencyRow2;
    @FXML private VBox securityOptions;

    private final ApplicationState state = ApplicationState.getInstance();
    private final AccountManager accountManager = state.getAccountManager();
    private Account currentAccount;
    private ImageView profileAvatarView;

    private static final String[] THEMES = {"Dark", "Light"};

    private static final String[][] CURRENCIES = {
        {"$ USD", "USD"},
        {"\u20AC EUR", "EUR"},
        {"EGP", "EGP"},
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(this::refresh);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(this::refresh);
    }

    private void refresh() {
        currentAccount = state.getCurrentAccount();
        if (currentAccount == null) return;
        AvatarHelper.setSidebarAvatar(sidebarAvatarContainer, currentAccount);
        applyTheme();
        renderProfile();
        renderThemeToggles();
        renderCurrencyToggles();
        renderSecurityActions();
    }

    private void applyTheme() {
        AppSettings.Mode mode = AppSettings.Mode.DARK;
        if (currentAccount.getAccountConfig() != null && currentAccount.getAccountConfig().getMode() != null) {
            mode = currentAccount.getAccountConfig().getMode();
        }
        App.setTheme(mode);
        if (mode == AppSettings.Mode.LIGHT) {
            rootPane.getStyleClass().remove("dark-mode");
            rootPane.getStyleClass().add("light-mode");
        } else {
            rootPane.getStyleClass().remove("light-mode");
            rootPane.getStyleClass().add("dark-mode");
        }
    }

    // ── FXML handlers ──

    @FXML
    private void handleEditProfile() {
        if (currentAccount == null) return;
        Account result = DialogHelper.showEditProfileDialog(currentAccount);
        if (result != null) {
            accountManager.updateAccountName(result, result.getUserName());
            if (result.getAccountConfig() != null) {
                accountManager.editAccount(result, result.getAccountConfig());
            }
            renderProfile();
        }
    }

    @FXML
    private void handleAddCategory() {
        DialogHelper.showNewCategoryDialog();
    }

    @FXML
    private void handleDeleteAccount() {
        if (currentAccount == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Account");
        confirm.setHeaderText("Permanently delete your account?");
        confirm.setContentText("This will delete all your transactions, budgets, goals, and account data. This cannot be undone.");
        confirm.getDialogPane().getStylesheets().add(
                getClass().getResource("styles.css").toExternalForm());
        confirm.getDialogPane().applyCss();
        DialogHelper.styleDialogButtons(confirm);
        confirm.getDialogPane().lookupButton(ButtonType.OK);
        if (confirm.showAndWait().orElse(null) == ButtonType.OK) {
            AppSettings.Message msg = accountManager.deleteAccount(currentAccount);
            if (msg == AppSettings.Message.SUCCESS) {
                state.clearSession();
                App.showLogin();
            } else {
                showError("Failed to delete account.");
            }
        }
    }

    private void handleChangePassword() {
        if (currentAccount == null) return;
        String[] passwords = DialogHelper.showChangePasswordDialog();
        if (passwords != null) {
            AppSettings.Message msg = accountManager.updatePassword(currentAccount, passwords[0], passwords[1]);
            if (msg == AppSettings.Message.SUCCESS) {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Password Changed");
                info.setHeaderText(null);
                info.setContentText("Your password has been updated successfully.");
                info.getDialogPane().getStylesheets().add(
                        getClass().getResource("styles.css").toExternalForm());
                info.showAndWait();
            } else {
                showError("Failed to change password. Check your current password.");
            }
        }
    }

    private void handleSignOut() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Sign Out");
        confirm.setHeaderText("Sign out of your account?");
        confirm.setContentText("You will need to log in again to access your data.");
        confirm.getDialogPane().getStylesheets().add(
                getClass().getResource("styles.css").toExternalForm());
        confirm.getDialogPane().applyCss();
        DialogHelper.styleDialogButtons(confirm);
        confirm.getDialogPane().lookupButton(ButtonType.OK);
        if (confirm.showAndWait().orElse(null) == ButtonType.OK) {
            state.clearSession();
            App.showLogin();
        }
    }

    @FXML private void navigateToDashboard() { App.showDashboard(); }
    @FXML private void navigateToTransactions() { App.showTransactions(); }
    @FXML private void navigateToBudgets() { App.showBudgets(); }
    @FXML private void navigateToGoals() { App.showGoals(); }
    @FXML private void navigateToReports() { App.showReports(); }
    @FXML private void navigateToProfile() { App.showProfile(); }

    // ── Render methods ──

    private void renderProfile() {
        profileNameLabel.setText(currentAccount.getUserName());
        profileEmailLabel.setText(currentAccount.getEmail());
        renderAvatar();
    }

    private void renderAvatar() {
        profileAvatarContainer.getChildren().clear();
        String avatarPath = currentAccount.getAccountConfig() != null
                ? currentAccount.getAccountConfig().getAvatarPath() : null;
        if (avatarPath != null && !avatarPath.isEmpty()) {
            try {
                Image img = new Image(new File(avatarPath).toURI().toString());
                profileAvatarView = new ImageView(img);
                profileAvatarView.setFitWidth(96);
                profileAvatarView.setFitHeight(96);
                profileAvatarView.setPreserveRatio(true);
                Circle clip = new Circle(48, 48, 48);
                profileAvatarView.setClip(clip);
                profileAvatarContainer.getChildren().add(profileAvatarView);
                return;
            } catch (Exception e) {
                System.err.println("Failed to load avatar: " + e.getMessage());
            }
        }
        Circle circle = new Circle(50);
        circle.setFill(javafx.scene.paint.Color.web("#1F2937"));
        SVGPath svg = new SVGPath();
        svg.setContent("M12 12c2.21 0 4-1.79 4-5s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z");
        svg.setFill(javafx.scene.paint.Color.web("#6B7280"));
        profileAvatarContainer.getChildren().addAll(circle, svg);
    }

    private void renderThemeToggles() {
        themeToggleBox.getChildren().clear();
        ToggleGroup group = new ToggleGroup();
        AppSettings.Mode currentMode = currentAccount.getAccountConfig() != null
                && currentAccount.getAccountConfig().getMode() != null
                ? currentAccount.getAccountConfig().getMode() : AppSettings.Mode.DARK;

        for (String label : THEMES) {
            ToggleButton btn = new ToggleButton(label);
            btn.getStyleClass().add("theme-toggle");
            btn.setToggleGroup(group);
            AppSettings.Mode mode = AppSettings.Mode.valueOf(label.toUpperCase());
            btn.setSelected(mode == currentMode);
            btn.setOnAction(e -> updateTheme(mode));
            themeToggleBox.getChildren().add(btn);
        }
    }

    private void renderCurrencyToggles() {
        currencyRow1.getChildren().clear();
        currencyRow2.getChildren().clear();
        ToggleGroup group = new ToggleGroup();
        AppSettings.Currency currentCurrency = currentAccount.getAccountConfig() != null
                && currentAccount.getAccountConfig().getCurrency() != null
                ? currentAccount.getAccountConfig().getCurrency() : AppSettings.Currency.USD;

        for (int i = 0; i < CURRENCIES.length; i++) {
            String displayLabel = CURRENCIES[i][0];
            String code = CURRENCIES[i][1];
            ToggleButton btn = new ToggleButton(displayLabel);
            btn.getStyleClass().add("currency-toggle");
            btn.setToggleGroup(group);
            AppSettings.Currency currency = AppSettings.Currency.valueOf(code);
            btn.setSelected(currency == currentCurrency);
            btn.setOnAction(e -> updateCurrency(currency));

            if (i < 2) {
                currencyRow1.getChildren().add(btn);
            } else {
                currencyRow2.getChildren().add(btn);
            }
        }
    }

    private void renderSecurityActions() {
        securityOptions.getChildren().clear();
        securityOptions.getChildren().add(buildSecurityRow(
                "Change Password", "Update your account password", "Change", "security-btn",
                this::handleChangePassword));
        securityOptions.getChildren().add(buildSecurityRow(
                "Sign Out", "Sign out from all devices", "Sign Out", "signout-btn",
                this::handleSignOut));
    }

    private HBox buildSecurityRow(String label, String description, String buttonText,
                                   String buttonStyle, Runnable handler) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("security-item");

        VBox info = new VBox();
        HBox.setHgrow(info, Priority.ALWAYS);
        Label title = new Label(label);
        title.getStyleClass().add("security-label");
        Label desc = new Label(description);
        desc.getStyleClass().add("security-desc");
        info.getChildren().addAll(title, desc);

        Button btn = new Button(buttonText);
        btn.getStyleClass().add(buttonStyle);
        btn.setOnAction(e -> handler.run());

        row.getChildren().addAll(info, btn);
        return row;
    }

    private void updateTheme(AppSettings.Mode newMode) {
        if (currentAccount == null) return;
        AccountConfig config = currentAccount.getAccountConfig();
        if (config == null) {
            config = new AccountConfig();
            currentAccount.setAccountConfig(config);
        }
        config.setMode(newMode);
        AppSettings.Message msg = accountManager.editAccount(currentAccount, config);
        if (msg == AppSettings.Message.SUCCESS) {
            applyTheme();
        } else {
            showError("Failed to update theme.");
        }
    }

    private void updateCurrency(AppSettings.Currency newCurrency) {
        if (currentAccount == null) return;
        AccountConfig config = currentAccount.getAccountConfig();
        if (config == null) {
            config = new AccountConfig();
            currentAccount.setAccountConfig(config);
        }
        config.setCurrency(newCurrency);
        AppSettings.Message msg = accountManager.editAccount(currentAccount, config);
        if (msg != AppSettings.Message.SUCCESS) {
            showError("Failed to update currency.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("styles.css").toExternalForm());
        alert.showAndWait();
    }
}
