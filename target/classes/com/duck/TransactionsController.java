package com.duck;

import com.duck.model.authentication.Session;
import com.duck.model.records.TransactionManager;
import com.duck.model.type.Account;
import com.duck.model.type.TransactionConfig;
import com.duck.model.type.AppSettings.TransactionType;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TransactionsController implements Initializable, PropertyChangeListener {

    @FXML
    private VBox transactionsContainer;
    @FXML
    private StackPane sidebarAvatarContainer;

    @FXML
    private Button filterAllBtn, filterIncomeBtn, filterExpenseBtn;

    private static final String SVG_SHOPPING  = "M7 18c-1.1 0-1.99.9-1.99 2S5.9 22 7 22s2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42c-.14 0-.25-.11-.25-.25l.03-.12.9-1.63h7.45c.75 0 1.41-.41 1.75-1.03l3.58-6.49c.08-.14.12-.31.12-.48 0-.55-.45-1-1-1H5.21l-.94-2H1zm16 16c-1.1 0-1.99.9-1.99 2s.89 2 1.99 2 2-.9 2-2-.9-2-2-2z";
    private static final String SVG_MONEY     = "M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.35 3.68 3.74V19h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z";
    private static final String SVG_FOOD      = "M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z";
    private static final String SVG_CAR       = "M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z";
    private static final String SVG_UTILITIES = "M20 4H4v2h16V4zm1 10v-2l-1-5H4l-1 5v2h1v6h10v-6h4v6h2v-6h1zm-9 4H6v-4h6v4z";

    private final ApplicationState state = ApplicationState.getInstance();
    private final TransactionManager transactionManager = state.getTransactionManager();
    private Account currentAccount;
    private List<DisplayTransaction> allTransactions = new ArrayList<>();
    private String activeFilter = "ALL";

    public static class DisplayTransaction {
        public String name;
        public String category;
        public double amount;
        public String dateGroup;
        public String svgPath;

        public DisplayTransaction(String name, String category, double amount, String dateGroup, String svgPath) {
            this.name = name;
            this.category = category;
            this.amount = amount;
            this.dateGroup = dateGroup;
            this.svgPath = svgPath;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        transactionManager.addPropertyChangeListener(this);
        loadCurrentAccount();
        loadTransactions();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        loadTransactions();
    }

    private void loadCurrentAccount() {
        String token = Session.getInstance().getToken();
        if (token == null) return;

        String email = extractEmailFromToken(token);
        if (email == null) return;

        for (Account acc : state.getStorage().getAccounts()) {
            if (acc.getEmail().equalsIgnoreCase(email)) {
                currentAccount = acc;
                break;
            }
        }
        AvatarHelper.setSidebarAvatar(sidebarAvatarContainer, currentAccount);
    }

    private String extractEmailFromToken(String token) {
        try {
            String[] parts = token.split("_");
            return parts.length >= 2 ? parts[1] : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void loadTransactions() {
        allTransactions.clear();

        if (currentAccount != null) {
            TransactionConfig config = new TransactionConfig(null, null, null, null, currentAccount);
            List<com.duck.model.type.Transaction> modelTxns = transactionManager.getTransactions(config);

            for (com.duck.model.type.Transaction t : modelTxns) {
                String svg = svgForCategory(t.getCategory());
                double displayAmount = t.getType() == TransactionType.EXPENSE ? -t.getAmount() : t.getAmount();
                allTransactions.add(new DisplayTransaction(
                        t.getCategory(),
                        t.getCategory(),
                        displayAmount,
                        dateGroup(t.getDate()),
                        svg
                ));
            }
        }

        applyFilter();
    }

    @FXML
    private void filterAll() {
        activeFilter = "ALL";
        updateFilterButtonStyles();
        applyFilter();
    }

    @FXML
    private void filterIncome() {
        activeFilter = "INCOME";
        updateFilterButtonStyles();
        applyFilter();
    }

    @FXML
    private void filterExpense() {
        activeFilter = "EXPENSE";
        updateFilterButtonStyles();
        applyFilter();
    }

    private void applyFilter() {
        List<DisplayTransaction> filtered;
        switch (activeFilter) {
            case "INCOME":
                filtered = allTransactions.stream()
                        .filter(t -> t.amount >= 0)
                        .collect(java.util.stream.Collectors.toList());
                break;
            case "EXPENSE":
                filtered = allTransactions.stream()
                        .filter(t -> t.amount < 0)
                        .collect(java.util.stream.Collectors.toList());
                break;
            default:
                filtered = new ArrayList<>(allTransactions);
        }
        renderTransactions(filtered);
    }

    private void updateFilterButtonStyles() {
        filterAllBtn.getStyleClass().removeAll("filter-btn-active", "filter-btn");
        filterIncomeBtn.getStyleClass().removeAll("filter-btn-active", "filter-btn");
        filterExpenseBtn.getStyleClass().removeAll("filter-btn-active", "filter-btn");

        filterAllBtn.getStyleClass().add("filter-btn");
        filterIncomeBtn.getStyleClass().add("filter-btn");
        filterExpenseBtn.getStyleClass().add("filter-btn");

        switch (activeFilter) {
            case "ALL":     filterAllBtn.getStyleClass().add("filter-btn-active"); break;
            case "INCOME":  filterIncomeBtn.getStyleClass().add("filter-btn-active"); break;
            case "EXPENSE": filterExpenseBtn.getStyleClass().add("filter-btn-active"); break;
        }
    }

    private String dateGroup(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.equals(today)) return "Today";
        if (date.equals(today.minusDays(1))) return "Yesterday";
        if (ChronoUnit.DAYS.between(date, today) <= 7) return "This Week";
        if (date.getMonth() == today.getMonth() && date.getYear() == today.getYear()) return "This Month";
        return date.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy"));
    }

    private String svgForCategory(String category) {
        if (category == null) return SVG_SHOPPING;
        String c = category.toLowerCase();
        if (c.contains("food") || c.contains("dining") || c.contains("grocery")) return SVG_FOOD;
        if (c.contains("transport") || c.contains("car") || c.contains("gas")) return SVG_CAR;
        if (c.contains("utility") || c.contains("internet") || c.contains("bill")) return SVG_UTILITIES;
        if (c.contains("salary") || c.contains("income") || c.contains("freelance")) return SVG_MONEY;
        return SVG_SHOPPING;
    }

    @FXML
    private void handleAddCategory() {
        DialogHelper.showNewCategoryDialog();
    }

    private void renderTransactions(List<DisplayTransaction> txns) {
        transactionsContainer.getChildren().clear();
        List<String> seenGroups = new ArrayList<>();
        for (DisplayTransaction t : txns) {
            if (!seenGroups.contains(t.dateGroup)) {
                seenGroups.add(t.dateGroup);
            }
        }
        for (String group : seenGroups) {
            VBox groupBox = new VBox(12);
            groupBox.getStyleClass().add("date-group");
            Label dateLabel = new Label(group);
            dateLabel.getStyleClass().add("date-header");
            groupBox.getChildren().add(dateLabel);
            for (DisplayTransaction t : txns) {
                if (t.dateGroup.equals(group)) {
                    groupBox.getChildren().add(buildTransactionRow(t));
                }
            }
            transactionsContainer.getChildren().add(groupBox);
        }
    }

    private HBox buildTransactionRow(DisplayTransaction t) {
        HBox row = new HBox(12);
        row.getStyleClass().add("transaction-row");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        javafx.scene.layout.StackPane iconPane = new javafx.scene.layout.StackPane();
        iconPane.getStyleClass().add("transaction-icon");
        Circle circle = new Circle(20);
        circle.setFill(javafx.scene.paint.Color.web("#1F2937"));
        SVGPath svg = new SVGPath();
        svg.setContent(t.svgPath);
        svg.getStyleClass().add("transaction-icon-svg");
        iconPane.getChildren().addAll(circle, svg);

        VBox info = new VBox(2);
        HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);
        Label name = new Label(t.name);
        name.getStyleClass().add("transaction-name");
        Label category = new Label(t.category);
        category.getStyleClass().add("transaction-category");
        info.getChildren().addAll(name, category);

        boolean isIncome = t.amount >= 0;
        Label amountLabel = new Label(
                (isIncome ? "+" : "") + String.format("$%.2f", t.amount)
        );
        amountLabel.getStyleClass().add(isIncome ? "amount-income" : "amount-expense");

        row.getChildren().addAll(iconPane, info, amountLabel);
        return row;
    }

    @FXML private void navigateToDashboard() { App.showDashboard(); }
    @FXML private void navigateToBudgets() { App.showBudgets(); }
    @FXML private void navigateToGoals() { App.showGoals(); }
    @FXML private void navigateToReports() { App.showReports(); }
    @FXML private void navigateToProfile() { App.showProfile(); }
}
