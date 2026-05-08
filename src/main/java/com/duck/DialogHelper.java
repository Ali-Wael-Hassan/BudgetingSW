package com.duck;

import com.duck.model.dataAccessors.LocalStorage;
import com.duck.model.records.Budget;
import com.duck.model.type.Account;
import com.duck.model.type.Period;
import com.duck.model.type.SavingGoal;
import com.duck.model.type.Transaction;
import com.duck.model.type.TransactionConfig;
import com.duck.model.type.AppSettings.DataKey;
import com.duck.model.type.AppSettings.TransactionType;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.scene.shape.Circle;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javafx.scene.control.Button;
import javafx.event.ActionEvent;

/**
 * Utility class with static methods for constructing and displaying modal
 * dialogs.  Provides dialogs for creating transactions, budgets, saving
 * goals, editing profiles, changing passwords, and adding categories.
 */
public class DialogHelper {

    // =========================================================================
    // Stylesheet Helpers
    // =========================================================================

    private static final String CSS = DialogHelper.class.getResource("styles.css").toExternalForm();
    private static String lightCss;

    /**
     * Lazily loads and returns the light theme stylesheet URL.
     * @return the light CSS URL, or null if not found
     */
    private static String getLightCss() {
        if (lightCss == null) {
            java.net.URL url = DialogHelper.class.getResource("theme-light.css");
            if (url != null) lightCss = url.toExternalForm();
        }
        return lightCss;
    }

    /**
     * Applies the application stylesheets (main CSS and light theme) to a dialog.
     * @param dialog the dialog to style
     */
    public static void addThemeStylesheets(Dialog<?> dialog) {
        dialog.getDialogPane().getStylesheets().add(CSS);
        String lc = getLightCss();
        if (lc != null) {
            dialog.getDialogPane().getStylesheets().add(lc);
        }
    }

    // =========================================================================
    // Transaction Dialog
    // =========================================================================

    /**
     * Shows a dialog to create a new transaction with no preset type.
     * @param account the account to associate the transaction with
     * @return a new Transaction, or null if cancelled
     */
    public static Transaction showTransactionDialog(Account account) {
        return showTransactionDialog(account, null);
    }

    /**
     * Shows a dialog to create a new transaction with an optional preset type.
     * Provides fields for type (Income / Expense), category, amount, and date.
     * @param account the account to associate the transaction with
     * @param preset  the preselected transaction type, or null for no default
     * @return a new Transaction, or null if the dialog was cancelled
     */
    public static Transaction showTransactionDialog(Account account, TransactionType preset) {
        if (account == null) return null;

        List<String> categories = LocalStorage.getInstance().getCategories();
        if (categories.isEmpty()) return null;

        Dialog<Transaction> dialog = new Dialog<>();
        dialog.setTitle("New Transaction");
        dialog.getDialogPane().getStylesheets().add(CSS);

        ToggleGroup typeGroup = new ToggleGroup();
        ToggleButton incomeBtn = new ToggleButton("Income");
        incomeBtn.setToggleGroup(typeGroup);
        incomeBtn.getStyleClass().add("filter-btn");
        ToggleButton expenseBtn = new ToggleButton("Expense");
        expenseBtn.setToggleGroup(typeGroup);
        expenseBtn.getStyleClass().add("filter-btn");
        if (preset == TransactionType.INCOME) incomeBtn.setSelected(true);
        else if (preset == TransactionType.EXPENSE) expenseBtn.setSelected(true);
        else incomeBtn.setSelected(true);
        HBox typeRow = new HBox(8, incomeBtn, expenseBtn);

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(categories);
        categoryCombo.getSelectionModel().select(0);
        categoryCombo.setMaxWidth(Double.MAX_VALUE);
        categoryCombo.getStyleClass().add("text-input");
        categoryCombo.setEditable(true);

        TextField amountField = new TextField();
        amountField.setPromptText("0.00");
        amountField.getStyleClass().add("text-input");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.getStyleClass().add("text-input");
        datePicker.setMaxWidth(Double.MAX_VALUE);

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-message");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);

        VBox form = new VBox(16);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("auth-card");

        form.getChildren().addAll(
                styledField("Type", typeRow),
                styledField("Category", categoryCombo),
                styledField("Amount ($)", amountField),
                styledField("Date", datePicker),
                errorLabel
        );

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(380);

        ButtonType createBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);
        dialog.getDialogPane().applyCss();
        dialog.getDialogPane().layout();
        styleDialogButtons(dialog);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(createBtn);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            try {
                String category = categoryCombo.getValue();
                LocalDate date = datePicker.getValue();
                if (category == null || category.trim().isEmpty()) {
                    showError(errorLabel, "Please select a category.");
                    event.consume(); return;
                }
                if (date == null) {
                    showError(errorLabel, "Please select a date.");
                    event.consume(); return;
                }
                if (date.isAfter(LocalDate.now())) {
                    showError(errorLabel, "Date cannot be in the future.");
                    event.consume(); return;
                }
                float amount = Float.parseFloat(amountField.getText().trim());
                if (amount <= 0) {
                    showError(errorLabel, "Amount must be greater than zero.");
                    event.consume(); return;
                }
                TransactionType type = incomeBtn.isSelected() ? TransactionType.INCOME : TransactionType.EXPENSE;
                if (!categories.contains(category))
                    LocalStorage.getInstance().insert(DataKey.CATEGORIES, category);
                Period period = new Period(date.withDayOfMonth(1), date.withDayOfMonth(date.lengthOfMonth()));
                TransactionConfig config = new TransactionConfig(type, period, List.of(category), null, account);
                dialog.setResult(new Transaction(config, date, amount));
            } catch (NumberFormatException e) {
                showError(errorLabel, "Amount must be a valid number.");
                event.consume();
            }
        });
        dialog.setResultConverter(btn -> btn == createBtn ? dialog.getResult() : null);

        Optional<Transaction> result = dialog.showAndWait();
        return result.orElse(null);
    }

    // =========================================================================
    // Budget Dialog
    // =========================================================================

    /**
     * Shows a dialog to create a new budget with category, amount, date
     * range, and alert threshold fields.
     * @param account the account to associate the budget with
     * @return a new Budget, or null if cancelled
     */
    public static Budget showBudgetDialog(Account account) {
        if (account == null) return null;

        List<String> categories = LocalStorage.getInstance().getCategories();
        if (categories.isEmpty()) return null;

        Dialog<Budget> dialog = new Dialog<>();
        dialog.setTitle("New Budget");
        dialog.getDialogPane().getStylesheets().add(CSS);

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(categories);
        categoryCombo.getSelectionModel().select(0);
        categoryCombo.setMaxWidth(Double.MAX_VALUE);
        categoryCombo.getStyleClass().add("text-input");
        categoryCombo.setEditable(true);

        TextField amountField = new TextField();
        amountField.setPromptText("0.00");
        amountField.getStyleClass().add("text-input");

        DatePicker startPicker = new DatePicker(LocalDate.now().withDayOfMonth(1));
        startPicker.getStyleClass().add("text-input");
        startPicker.setMaxWidth(Double.MAX_VALUE);

        DatePicker endPicker = new DatePicker(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));
        endPicker.getStyleClass().add("text-input");
        endPicker.setMaxWidth(Double.MAX_VALUE);

        TextField thresholdField = new TextField("0.75");
        thresholdField.setPromptText("e.g. 0.75 = 75%");
        thresholdField.getStyleClass().add("text-input");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-message");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);

        VBox form = new VBox(16);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("auth-card");

        form.getChildren().addAll(
                styledField("Category", categoryCombo),
                styledField("Budget Limit ($)", amountField),
                styledField("Start Date", startPicker),
                styledField("End Date", endPicker),
                styledField("Alert Threshold", thresholdField),
                errorLabel
        );

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(380);

        ButtonType createBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);
        dialog.getDialogPane().applyCss();
        dialog.getDialogPane().layout();
        styleDialogButtons(dialog);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(createBtn);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            try {
                String category = categoryCombo.getValue();
                LocalDate start = startPicker.getValue();
                LocalDate end = endPicker.getValue();
                if (category == null || category.trim().isEmpty()) {
                    showError(errorLabel, "Please select or enter a category.");
                    event.consume(); return;
                }
                if (start == null) {
                    showError(errorLabel, "Please select a start date.");
                    event.consume(); return;
                }
                if (end == null) {
                    showError(errorLabel, "Please select an end date.");
                    event.consume(); return;
                }
                if (end.isBefore(start)) {
                    showError(errorLabel, "Start date must be before end date.");
                    event.consume(); return;
                }
                float amount = Float.parseFloat(amountField.getText().trim());
                if (amount <= 0) {
                    showError(errorLabel, "Budget amount must be greater than zero.");
                    event.consume(); return;
                }
                float threshold;
                try {
                    threshold = Float.parseFloat(thresholdField.getText().trim());
                    if (threshold <= 0) threshold = 0.75f;
                } catch (NumberFormatException e) {
                    showError(errorLabel, "Alert threshold must be a valid number.");
                    event.consume(); return;
                }
                if (!categories.contains(category))
                    LocalStorage.getInstance().insert(DataKey.CATEGORIES, category);
                Period period = new Period(start, end);
                Budget budget = new Budget(category, amount, period, threshold);
                budget.setAccount(account);
                dialog.setResult(budget);
            } catch (NumberFormatException e) {
                showError(errorLabel, "Budget amount must be a valid number.");
                event.consume();
            }
        });
        dialog.setResultConverter(btn -> btn == createBtn ? dialog.getResult() : null);

        Optional<Budget> result = dialog.showAndWait();
        return result.orElse(null);
    }

    // =========================================================================
    // Saving Goal Dialog
    // =========================================================================

    /**
     * Shows a dialog to create a new saving goal with name, target amount,
     * current amount, and deadline fields.
     * @param account the account to associate the goal with
     * @return a new SavingGoal, or null if cancelled
     */
    public static SavingGoal showGoalDialog(Account account) {
        if (account == null) return null;

        Dialog<SavingGoal> dialog = new Dialog<>();
        dialog.setTitle("New Saving Goal");
        dialog.getDialogPane().getStylesheets().add(CSS);

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. New Laptop");
        nameField.getStyleClass().add("text-input");

        TextField targetField = new TextField();
        targetField.setPromptText("0.00");
        targetField.getStyleClass().add("text-input");

        TextField currentField = new TextField("0");
        currentField.setPromptText("0.00");
        currentField.getStyleClass().add("text-input");

        DatePicker deadlinePicker = new DatePicker(LocalDate.now().plusMonths(1));
        deadlinePicker.getStyleClass().add("text-input");
        deadlinePicker.setMaxWidth(Double.MAX_VALUE);

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-message");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);

        VBox form = new VBox(16);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("auth-card");

        form.getChildren().addAll(
                styledField("Goal Name", nameField),
                styledField("Target Amount ($)", targetField),
                styledField("Current Amount ($)", currentField),
                styledField("Deadline", deadlinePicker),
                errorLabel
        );

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(380);

        ButtonType createBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);
        dialog.getDialogPane().applyCss();
        dialog.getDialogPane().layout();
        styleDialogButtons(dialog);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(createBtn);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    showError(errorLabel, "Please enter a goal name.");
                    event.consume(); return;
                }
                float target = Float.parseFloat(targetField.getText().trim());
                if (target <= 0) {
                    showError(errorLabel, "Target amount must be greater than zero.");
                    event.consume(); return;
                }
                float current = Float.parseFloat(currentField.getText().trim());
                if (current < 0) {
                    showError(errorLabel, "Current amount cannot be negative.");
                    event.consume(); return;
                }
                if (current > target) {
                    showError(errorLabel, "Current amount cannot exceed the target amount.");
                    event.consume(); return;
                }
                LocalDate deadline = deadlinePicker.getValue();
                if (deadline == null) {
                    showError(errorLabel, "Please select a deadline.");
                    event.consume(); return;
                }
                if (deadline.isBefore(LocalDate.now())) {
                    showError(errorLabel, "Deadline must be in the future.");
                    event.consume(); return;
                }
                dialog.setResult(new SavingGoal(name, target, current, deadline, account));
            } catch (NumberFormatException e) {
                showError(errorLabel, "Please enter valid numbers for amounts.");
                event.consume();
            }
        });
        dialog.setResultConverter(btn -> btn == createBtn ? dialog.getResult() : null);

        Optional<SavingGoal> result = dialog.showAndWait();
        return result.orElse(null);
    }

    // =========================================================================
    // Change Password Dialog
    // =========================================================================

    /**
     * Shows a dialog to change the account password.  Requires the current
     * password, new password, and confirmation.
     * @return a String array with [oldPassword, newPassword], or null if cancelled
     */
    public static String[] showChangePasswordDialog() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.getDialogPane().getStylesheets().add(CSS);

        PasswordField oldField = new PasswordField();
        oldField.setPromptText("Current password");
        oldField.getStyleClass().add("text-input");

        PasswordField newField = new PasswordField();
        newField.setPromptText("New password");
        newField.getStyleClass().add("text-input");

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm new password");
        confirmField.getStyleClass().add("text-input");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-message");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);

        VBox form = new VBox(16);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("auth-card");
        form.getChildren().addAll(
                styledField("Current Password", oldField),
                styledField("New Password", newField),
                styledField("Confirm New Password", confirmField),
                errorLabel
        );

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(380);

        ButtonType changeBtn = new ButtonType("Change", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeBtn, ButtonType.CANCEL);
        dialog.getDialogPane().applyCss();
        dialog.getDialogPane().layout();
        styleDialogButtons(dialog);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(changeBtn);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            String oldPwd = oldField.getText();
            String newPwd = newField.getText();
            String confirm = confirmField.getText();
            if (oldPwd.isEmpty() || newPwd.isEmpty() || confirm.isEmpty()) {
                showError(errorLabel, "All fields are required.");
                event.consume(); return;
            }
            if (!newPwd.equals(confirm)) {
                showError(errorLabel, "New passwords do not match.");
                event.consume(); return;
            }
            if (newPwd.length() < 6) {
                showError(errorLabel, "Password must be at least 6 characters.");
                event.consume(); return;
            }
            dialog.setResult(new String[]{oldPwd, newPwd});
        });
        dialog.setResultConverter(btn -> btn == changeBtn ? dialog.getResult() : null);

        java.util.Optional<String[]> result = dialog.showAndWait();
        return result.orElse(null);
    }

    // =========================================================================
    // Edit Profile Dialog
    // =========================================================================

    /**
     * Shows a dialog to edit the user profile.  Allows changing the display
     * name and profile photo via a file chooser.
     * @param account the account to edit
     * @return the updated Account, or null if cancelled
     */
    public static Account showEditProfileDialog(Account account) {
        if (account == null) return null;

        Dialog<Account> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");
        dialog.getDialogPane().getStylesheets().add(CSS);

        TextField nameField = new TextField(account.getUserName());
        nameField.setPromptText("Your name");
        nameField.getStyleClass().add("text-input");

        StackPane avatarPreview = new StackPane();
        avatarPreview.setPrefSize(80, 80);
        ImageView avatarView = new ImageView();
        avatarView.setFitWidth(76);
        avatarView.setFitHeight(76);
        avatarView.setPreserveRatio(true);
        Circle clip = new Circle(38, 38, 38);
        avatarView.setClip(clip);

        String existingPath = account.getAccountConfig() != null ? account.getAccountConfig().getAvatarPath() : null;
        if (existingPath != null && !existingPath.isEmpty()) {
            try {
                avatarView.setImage(new javafx.scene.image.Image(new java.io.File(existingPath).toURI().toString()));
                avatarPreview.getChildren().add(avatarView);
            } catch (Exception e) {
                avatarPreview.getChildren().add(createDefaultAvatarSvg());
            }
        } else {
            avatarPreview.getChildren().add(createDefaultAvatarSvg());
        }

        Button browseBtn = new Button("Change Photo");
        browseBtn.getStyleClass().add("edit-profile-btn");
        browseBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choose Profile Picture");
            fc.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
            java.io.File file = fc.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                try {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(file.toURI().toString());
                    avatarView.setImage(img);
                    avatarPreview.getChildren().setAll(avatarView);

                    if (account.getAccountConfig() == null) {
                        account.setAccountConfig(new com.duck.model.type.AccountConfig());
                    }
                    account.getAccountConfig().setAvatarPath(file.getAbsolutePath());
                } catch (Exception ex) {
                    System.err.println("Failed to load image: " + ex.getMessage());
                }
            }
        });

        VBox avatarSection = new VBox(8, avatarPreview, browseBtn);
        avatarSection.setAlignment(javafx.geometry.Pos.CENTER);

        VBox form = new VBox(16);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("auth-card");
        form.getChildren().addAll(avatarSection, styledField("Display Name", nameField));

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(380);

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);
        dialog.getDialogPane().applyCss();
        dialog.getDialogPane().layout();
        styleDialogButtons(dialog);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(saveBtn);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                event.consume(); return;
            }
            if (account.getAccountConfig() == null) {
                account.setAccountConfig(new com.duck.model.type.AccountConfig());
            }
            account.setUserName(name);
            dialog.setResult(account);
        });
        dialog.setResultConverter(btn -> btn == saveBtn ? dialog.getResult() : null);

        java.util.Optional<Account> result = dialog.showAndWait();
        return result.orElse(null);
    }

    // =========================================================================
    // Avatar Helper
    // =========================================================================

    /**
     * Creates a default avatar placeholder SVG (person silhouette).
     * @return a StackPane containing the default avatar graphic
     */
    private static StackPane createDefaultAvatarSvg() {
        StackPane sp = new StackPane();
        sp.getStyleClass().add("default-avatar");
        Circle circle = new Circle(40);
        javafx.scene.shape.SVGPath svg = new javafx.scene.shape.SVGPath();
        svg.setContent("M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z");
        svg.setFill(javafx.scene.paint.Color.web("#6B7280"));
        sp.getChildren().addAll(circle, svg);
        return sp;
    }

    // =========================================================================
    // New Category Dialog
    // =========================================================================

    /**
     * Shows a dialog to create a new transaction category.  The category
     * is saved to local storage if it does not already exist.
     */
    public static void showNewCategoryDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("New Category");
        dialog.getDialogPane().getStylesheets().add(CSS);

        TextField nameField = new TextField();
        nameField.setPromptText("Category name");
        nameField.getStyleClass().add("text-input");

        VBox form = new VBox(16);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("auth-card");
        form.getChildren().add(styledField("Category Name", nameField));

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(320);

        ButtonType createBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);
        dialog.getDialogPane().applyCss();
        dialog.getDialogPane().layout();
        styleDialogButtons(dialog);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(createBtn);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                event.consume(); return;
            }
            dialog.setResult(name);
        });
        dialog.setResultConverter(btn -> btn == createBtn ? dialog.getResult() : null);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!LocalStorage.getInstance().getCategories().contains(name))
                LocalStorage.getInstance().insert(DataKey.CATEGORIES, name);
        });
    }

    // =========================================================================
    // Dialog Styling Helpers
    // =========================================================================

    /**
     * Applies the dialog-button style class to all buttons in a dialog.
     * @param dialog the dialog whose buttons should be styled
     */
    public static void styleDialogButtons(Dialog<?> dialog) {
        dialog.getDialogPane().getButtonTypes().forEach(btnType -> {
            Button btn = (Button) dialog.getDialogPane().lookupButton(btnType);
            if (btn != null) {
                btn.getStyleClass().add("dialog-button");
            }
        });
    }

    /**
     * Shows an error message on a dialog label.
     * @param errorLabel the label to display the error on
     * @param message    the error text to show
     */
    private static void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    /**
     * Wraps a label and an input node in a styled VBox column.
     * @param labelText the text for the label
     * @param input     the input control (TextField, ComboBox, DatePicker, etc.)
     * @return a VBox containing the label and input
     */
    private static VBox styledField(String labelText, javafx.scene.Node input) {
        VBox box = new VBox(6);
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        box.getChildren().addAll(label, input);
        return box;
    }
}
