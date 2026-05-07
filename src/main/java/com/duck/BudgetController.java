package com.duck;

import com.duck.model.authentication.Session;
import com.duck.model.type.Account;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class BudgetController implements Initializable, PropertyChangeListener {

    // ── FXML injections ────────────────────────────────────────────
    @FXML private Label     monthLabel;
    @FXML private Label     summaryTotalLabel;
    @FXML private Label     summarySpentLabel;
    @FXML private Label     summaryRemainingLabel;
    @FXML private StackPane sidebarAvatarContainer;
    @FXML private StackPane mainProgressFill;
    @FXML private GridPane  categoriesGrid;
    @FXML private ScrollPane contentScroll;

    // ── SVG icon paths ─────────────────────────────────────────────
    private static final String SVG_HOUSE   = "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z";
    private static final String SVG_FOOD    = "M11 9H9V2H7v7H5V2H3v7c0 2.12 1.66 3.84 3.75 3.97V22h2.5v-9.03C11.34 12.84 13 11.12 13 9V2h-2v7zm5-3v8h2.5v8H21V2c-2.76 0-5 2.24-5 4z";
    private static final String SVG_CAR     = "M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z";
    private static final String SVG_UTILITY = "M12 3L2 12h3v8h6v-6h2v6h6v-8h3L12 3zm5 15h-2v-6H9v6H7v-7.81l5-4.5 5 4.5V18z";
    private static final String SVG_TV      = "M21 3H3c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h5v2h8v-2h5c1.1 0 1.99-.9 1.99-2L23 5c0-1.1-.9-2-2-2zm0 14H3V5h18v12z";
    private static final String SVG_SHOP    = "M7 18c-1.1 0-1.99.9-1.99 2S5.9 22 7 22s2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42c-.14 0-.25-.11-.25-.25l.03-.12.9-1.63h7.45c.75 0 1.41-.41 1.75-1.03l3.58-6.49c.08-.14.12-.31.12-.48 0-.55-.45-1-1-1H5.21l-.94-2H1zm16 16c-1.1 0-1.99.9-1.99 2s.89 2 1.99 2 2-.9 2-2-.9-2-2-2z";

    // ── Data model ─────────────────────────────────────────────────
    public static class BudgetCategory {
        public String name;
        public double spent;
        public double limit;
        public String svgPath;

        public BudgetCategory(String name, double spent, double limit, String svgPath) {
            this.name    = name;
            this.spent   = spent;
            this.limit   = limit;
            this.svgPath = svgPath;
        }

        public double percent() { return limit > 0 ? (spent / limit) * 100.0 : 0; }
        public boolean isOver() { return spent > limit; }
    }

    private final List<BudgetCategory> categories = new ArrayList<>();
    private static final int COLUMNS = 3;

    private Account currentAccount;

    private LocalDate currentMonth = LocalDate.now();
    private static final DateTimeFormatter MONTH_FMT =
            DateTimeFormatter.ofPattern("MMMM yyyy");

    // ── Lifecycle ──────────────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ApplicationState state = ApplicationState.getInstance();
        state.getBudgetController().addPropertyChangeListener(this);
        currentAccount = state.getCurrentAccount();
        AvatarHelper.setSidebarAvatar(sidebarAvatarContainer, currentAccount);
        monthLabel.setText(currentMonth.format(MONTH_FMT));
        applyTheme();
        loadBudgets();
        render();
    }

    private void applyTheme() {
        if (currentAccount == null) return;
        com.duck.model.type.AppSettings.Mode mode = com.duck.model.type.AppSettings.Mode.DARK;
        if (currentAccount.getAccountConfig() != null && currentAccount.getAccountConfig().getMode() != null) {
            mode = currentAccount.getAccountConfig().getMode();
        }
        App.setTheme(mode);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        loadBudgets();
        render();
    }

    // ── FXML handlers ──────────────────────────────────────────────

    @FXML
    private void handleAddBudget() {
        ApplicationState state = ApplicationState.getInstance();
        com.duck.model.records.Budget budget = DialogHelper.showBudgetDialog(state.getCurrentAccount());
        if (budget != null) {
            com.duck.model.type.AppSettings.Message result = state.getBudgetController().createBudget(budget);
            if (result == com.duck.model.type.AppSettings.Message.SUCCESS) {
                loadBudgets();
                render();
            } else {
                String error;
                switch (result) {
                    case INVALID_BUDGET_AMOUNT: error = "Budget amount must be greater than zero."; break;
                    case MULTIPLE_ACTIVE_BUDGETS_ERROR: error = "A budget for this category already exists in this period."; break;
                    case INVALID_PERIOD: error = "Start date must be before end date."; break;
                    case INVALID_THRESHOLD: error = "Alert threshold must be greater than zero."; break;
                    case NEGATIVE_USED_AMOUNT: error = "Used amount cannot be negative."; break;
                    default: error = "Failed to create budget. Please try again.";
                }
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Validation Error");
                alert.setHeaderText(null);
                alert.setContentText(error);
                alert.getDialogPane().getStylesheets().add(
                        getClass().getResource("styles.css").toExternalForm());
                alert.getDialogPane().applyCss();
                alert.getDialogPane().lookupButton(ButtonType.OK);
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void handlePrevMonth() {
        currentMonth = currentMonth.minusMonths(1);
        monthLabel.setText(currentMonth.format(MONTH_FMT));
        loadBudgets();
        render();
    }

    @FXML
    private void handleNextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        monthLabel.setText(currentMonth.format(MONTH_FMT));
        loadBudgets();
        render();
    }

    @FXML private void navigateToDashboard() { App.showDashboard(); }
    @FXML private void navigateToTransactions() { App.showTransactions(); }
    @FXML private void navigateToBudgets() { App.showBudgets(); }
    @FXML private void navigateToGoals() { App.showGoals(); }
    @FXML private void navigateToReports() { App.showReports(); }
    @FXML private void navigateToProfile() { App.showProfile(); }

    private void loadBudgets() {
        categories.clear();
        String token = Session.getInstance().getToken();
        if (token == null) return;
        try {
            String[] parts = token.split("_");
            String email = parts.length >= 2 ? parts[1] : null;
            if (email == null) return;
            for (Account acc : ApplicationState.getInstance().getStorage().getAccounts()) {
                if (acc.getEmail().equalsIgnoreCase(email)) {
                    currentAccount = acc;
                    break;
                }
            }
        } catch (Exception ignored) {}
        if (currentAccount == null) return;

        YearMonth selectedMonth = YearMonth.from(currentMonth);
        LocalDate monthStart = selectedMonth.atDay(1);
        LocalDate monthEnd = selectedMonth.atEndOfMonth();

        List<com.duck.model.records.Budget> modelBudgets =
                ApplicationState.getInstance().getBudgetController().getAllBudgets(currentAccount);
        for (com.duck.model.records.Budget b : modelBudgets) {
            com.duck.model.type.Period p = b.getPeriod();
            if (p == null) continue;
            if (p.getEndDate().isBefore(monthStart) || p.getStartDate().isAfter(monthEnd))
                continue;
            categories.add(new BudgetCategory(
                    b.getCategory(),
                    b.getUsedAmount(),
                    b.getAmount(),
                    svgForCategory(b.getCategory())
            ));
        }
    }

    private String svgForCategory(String category) {
        if (category == null) return SVG_SHOP;
        String c = category.toLowerCase();
        if (c.contains("food") || c.contains("dining") || c.contains("grocery")) return SVG_FOOD;
        if (c.contains("transport") || c.contains("car") || c.contains("gas")) return SVG_CAR;
        if (c.contains("utility") || c.contains("internet") || c.contains("bill")) return SVG_UTILITY;
        if (c.contains("entertainment") || c.contains("tv") || c.contains("movie")) return SVG_TV;
        if (c.contains("housing") || c.contains("rent") || c.contains("home")) return SVG_HOUSE;
        return SVG_SHOP;
    }

    // ── Rendering ──────────────────────────────────────────────────

    private void render() {
        updateSummary();
        renderGrid();
    }

    private void updateSummary() {
        double totalLimit = categories.stream().mapToDouble(c -> c.limit).sum();
        double totalSpent = categories.stream().mapToDouble(c -> c.spent).sum();
        double remaining  = totalLimit - totalSpent;

        summaryTotalLabel.setText(String.format("$%.2f", totalLimit));
        summarySpentLabel.setText(String.format("$%.2f", totalSpent));
        summaryRemainingLabel.setText(String.format("$%.2f", remaining));

        double pct = totalLimit > 0 ? Math.min(totalSpent / totalLimit, 1.0) : 0;
        mainProgressFill.maxWidthProperty().bind(Bindings.createDoubleBinding(() -> {
            Parent p = mainProgressFill.getParent();
            if (p instanceof Region) {
                return ((Region) p).getWidth() * pct;
            }
            return 0.0;
        }, mainProgressFill.parentProperty()));
    }

    private void renderGrid() {
        categoriesGrid.getChildren().clear();
        categoriesGrid.getColumnConstraints().clear();

        for (int col = 0; col < COLUMNS; col++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / COLUMNS);
            cc.setHgrow(Priority.ALWAYS);
            categoriesGrid.getColumnConstraints().add(cc);
        }

        for (int i = 0; i < categories.size(); i++) {
            VBox card = buildCard(categories.get(i));
            GridPane.setColumnIndex(card, i % COLUMNS);
            GridPane.setRowIndex(card, i / COLUMNS);
            categoriesGrid.getChildren().add(card);
        }
    }

    // ── Card builder ───────────────────────────────────────────────

    private VBox buildCard(BudgetCategory cat) {
        VBox card = new VBox(8);
        card.getStyleClass().add("category-card");

        // ── Icon + name row
        HBox nameRow = new HBox(12);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        StackPane iconPane = new StackPane();
        iconPane.getStyleClass().add("category-icon");
        Circle circle = new Circle(18);
        SVGPath svg = new SVGPath();
        svg.setContent(cat.svgPath);
        svg.getStyleClass().add("category-icon-svg");
        iconPane.getChildren().addAll(circle, svg);

        Label nameLabel = new Label(cat.name);
        nameLabel.getStyleClass().add("category-name");

        nameRow.getChildren().addAll(iconPane, nameLabel);

        // ── Spent / limit row
        HBox amountRow = new HBox(8);
        amountRow.setAlignment(Pos.CENTER_LEFT);
        amountRow.getStyleClass().add("category-amount-row");

        Label spentLabel = new Label(String.format("$%.0f", cat.spent));
        spentLabel.getStyleClass().add("category-spent");

        Label limitLabel = new Label(String.format("/ $%.0f", cat.limit));
        limitLabel.getStyleClass().add(cat.isOver() ? "category-limit-over" : "category-limit");

        amountRow.getChildren().addAll(spentLabel, limitLabel);

        // ── Progress bar
        StackPane progressContainer = new StackPane();
        progressContainer.getStyleClass().add("category-progress-container");

        StackPane progressBg = new StackPane();
        progressBg.getStyleClass().add("category-progress-bg");

        StackPane progressFill = new StackPane();
        String fillStyle = cat.isOver()               ? "category-progress-fill-red"
                         : cat.percent() >= 75        ? "category-progress-fill-yellow"
                         :                              "category-progress-fill-green";
        progressFill.getStyleClass().add(fillStyle);

        double clampedPct = Math.min(cat.percent() / 100.0, 1.0);
        progressFill.maxWidthProperty().bind(progressBg.widthProperty().multiply(clampedPct));

        progressBg.getChildren().add(progressFill);
        progressContainer.getChildren().add(progressBg);

        // ── Percentage label
        String pctText = String.format("%.0f%%", cat.percent());
        Label pctLabel = new Label(pctText);
        pctLabel.getStyleClass().add(cat.isOver() ? "category-percent-over" : "category-percent");

        card.getChildren().addAll(nameRow, amountRow, progressContainer, pctLabel);
        return card;
    }
}
