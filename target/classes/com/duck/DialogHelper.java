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
import javafx.geometry.Pos;
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
import javafx.scene.image.Image;
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

public class DialogHelper {

    private static final String CSS = DialogHelper.class.getResource("styles.css").toExternalForm();

    public static Transaction showTransactionDialog(Account account) {
        return showTransactionDialog(account, null);
    }

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

        DatePicker periodStartPicker = new DatePicker(LocalDate.now().withDayOfMonth(1));
        periodStartPicker.getStyleClass().add("text-input");
        periodStartPicker.setMaxWidth(Double.MAX_VALUE);

        DatePicker periodEndPicker = new DatePicker(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));
        periodEndPicker.getStyleClass().add("text-input");
        periodEndPicker.setMaxWidth(Double.MAX_VALUE);

        VBox form = new VBox(16);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("auth-card");

        form.getChildren().addAll(
                styledField("Type", typeRow),
                styledField("Category", categoryCombo),
                styledField("Amount ($)", amountField),
                styledField("Date", datePicker),
                styledField("Period Start", periodStartPicker),
                styledField("Period End", periodEndPicker)
        );

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(380);

        ButtonType createBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn != createBtn) return null;
            try {
                float amount = Float.parseFloat(amountField.getText().trim());
                if (amount <= 0) return null;
                TransactionType type = incomeBtn.isSelected() ? TransactionType.INCOME : TransactionType.EXPENSE;
                String category = categoryCombo.getValue();
                LocalDate date = datePicker.getValue();
                LocalDate pStart = periodStartPicker.getValue();
                LocalDate pEnd = periodEndPicker.getValue();
                if (category == null || date == null || pStart == null || pEnd == null) return null;
                if (!categories.contains(category))
                    LocalStorage.getInstance().insert(DataKey.CATEGORIES, category);
                Period period = new Period(pStart, pEnd);
                TransactionConfig config = new TransactionConfig(type, period, List.of(category), null, account);
                return new Transaction(config, date, amount);
            } catch (NumberFormatException e) {
                return null;
            }
        });

        Optional<Transaction> result = dialog.showAndWait();
        return result.orElse(null);
    }

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

        VBox form = new VBox(16);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("auth-card");

        form.getChildren().addAll(
                styledField("Category", categoryCombo),
                styledField("Budget Limit ($)", amountField),
                styledField("Start Date", startPicker),
                styledField("End Date", endPicker),
                styledField("Alert Threshold", thresholdField)
        );

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(380);

        ButtonType createBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn != createBtn) return null;
            try {
                float amount = Float.parseFloat(amountField.getText().trim());
                if (amount <= 0) return null;
                float threshold = Float.parseFloat(thresholdField.getText().trim());
                if (threshold <= 0) threshold = 0.75f;
                String category = categoryCombo.getValue();
                LocalDate start = startPicker.getValue();
                LocalDate end = endPicker.getValue();
                if (category == null || start == null || end == null) return null;
                if (!categories.contains(category))
                    LocalStorage.getInstance().insert(DataKey.CATEGORIES, category);
                Period period = new Period(start, end);
                Budget budget = new Budget(category, amount, period, threshold);
                budget.setAccount(account);
                return budget;
            } catch (NumberFormatException e) {
                return null;
            }
        });

        Optional<Budget> result = dialog.showAndWait();
        return result.orElse(null);
    }

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

        VBox form = new VBox(16);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("auth-card");

        form.getChildren().addAll(
                styledField("Goal Name", nameField),
                styledField("Target Amount ($)", targetField),
                styledField("Current Amount ($)", currentField),
                styledField("Deadline", deadlinePicker)
        );

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(380);

        ButtonType createBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn != createBtn) return null;
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) return null;
                float target = Float.parseFloat(targetField.getText().trim());
                if (target <= 0) return null;
                float current = Float.parseFloat(currentField.getText().trim());
                if (current < 0) return null;
                LocalDate deadline = deadlinePicker.getValue();
                if (deadline == null || deadline.isBefore(LocalDate.now())) return null;
                return new SavingGoal(name, target, current, deadline, account);
            } catch (NumberFormatException e) {
                return null;
            }
        });

        Optional<SavingGoal> result = dialog.showAndWait();
        return result.orElse(null);
    }

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

        VBox form = new VBox(16);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("auth-card");
        form.getChildren().addAll(
                styledField("Current Password", oldField),
                styledField("New Password", newField),
                styledField("Confirm New Password", confirmField)
        );

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(380);

        ButtonType changeBtn = new ButtonType("Change", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeBtn, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn != changeBtn) return null;
            String oldPwd = oldField.getText();
            String newPwd = newField.getText();
            String confirm = confirmField.getText();
            if (oldPwd.isEmpty() || newPwd.isEmpty() || confirm.isEmpty()) return null;
            if (!newPwd.equals(confirm)) return null;
            return new String[]{oldPwd, newPwd};
        });

        java.util.Optional<String[]> result = dialog.showAndWait();
        return result.orElse(null);
    }

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

        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;
            String name = nameField.getText().trim();
            if (name.isEmpty()) return null;
            if (account.getAccountConfig() == null) {
                account.setAccountConfig(new com.duck.model.type.AccountConfig());
            }
            account.setUserName(name);
            return account;
        });

        java.util.Optional<Account> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private static StackPane createDefaultAvatarSvg() {
        StackPane sp = new StackPane();
        Circle circle = new Circle(40);
        circle.setFill(javafx.scene.paint.Color.web("#1F2937"));
        javafx.scene.shape.SVGPath svg = new javafx.scene.shape.SVGPath();
        svg.setContent("M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z");
        svg.setFill(javafx.scene.paint.Color.web("#6B7280"));
        sp.getChildren().addAll(circle, svg);
        return sp;
    }

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

        dialog.setResultConverter(btn -> {
            if (btn != createBtn) return null;
            String name = nameField.getText().trim();
            return name.isEmpty() ? null : name;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!LocalStorage.getInstance().getCategories().contains(name))
                LocalStorage.getInstance().insert(DataKey.CATEGORIES, name);
        });
    }

    private static VBox styledField(String labelText, javafx.scene.Node input) {
        VBox box = new VBox(6);
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        box.getChildren().addAll(label, input);
        return box;
    }
}
