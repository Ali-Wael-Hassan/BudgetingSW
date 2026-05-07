package com.duck;

import com.duck.model.accountOps.PDFReport;
import com.duck.model.records.TransactionManager;
import com.duck.model.type.Account;
import com.duck.model.type.Period;
import com.duck.model.type.SavingGoal;
import com.duck.model.type.Transaction;
import com.duck.model.type.TransactionConfig;
import com.duck.model.type.AppSettings.TransactionType;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ReportsController implements Initializable, PropertyChangeListener {

    @FXML private HBox  cashFlowChart;
    @FXML private Label monthLabel;
    @FXML private StackPane sidebarAvatarContainer;
    @FXML private StackPane donutChart;
    @FXML private Label donutTotalLabel;
    @FXML private VBox  donutLegend;
    @FXML private VBox  expenseList;
    @FXML private VBox  goalsList;

    private YearMonth selectedMonth;
    private static final DateTimeFormatter MONTH_FMT =
            DateTimeFormatter.ofPattern("MMMM yyyy");

    private static final String SVG_HOUSE = "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z";
    private static final String SVG_CAR   = "M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z";
    private static final String SVG_FOOD  = "M11 9H9V2H7v7H5V2H3v7c0 2.12 1.66 3.84 3.75 3.97V22h2.5v-9.03C11.34 12.84 13 11.12 13 9V2h-2v7zm5-3v8h2.5v8H21V2c-2.76 0-5 2.24-5 4z";
    private static final String SVG_SHOP  = "M7 18c-1.1 0-1.99.9-1.99 2S5.9 22 7 22s2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42c-.14 0-.25-.11-.25-.25l.03-.12.9-1.63h7.45c.75 0 1.41-.41 1.75-1.03l3.58-6.49c.08-.14.12-.31.12-.48 0-.55-.45-1-1-1H5.21l-.94-2H1zm16 16c-1.1 0-1.99.9-1.99 2s.89 2 1.99 2 2-.9 2-2-.9-2-2-2z";
    private static final String SVG_UTILITY = "M12 3L2 12h3v8h6v-6h2v6h6v-8h3L12 3zm5 15h-2v-6H9v6H7v-7.81l5-4.5 5 4.5V18z";
    private static final String SVG_TV      = "M21 3H3c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h5v2h8v-2h5c1.1 0 1.99-.9 1.99-2L23 5c0-1.1-.9-2-2-2zm0 14H3V5h18v12z";

    private final ApplicationState state = ApplicationState.getInstance();
    private final TransactionManager transactionManager = state.getTransactionManager();
    private Account currentAccount;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        transactionManager.addPropertyChangeListener(this);
        state.getBudgetController().addPropertyChangeListener(this);
        state.getGoalsController().addPropertyChangeListener(this);
        selectedMonth = YearMonth.now();
        if (monthLabel != null) monthLabel.setText(selectedMonth.format(MONTH_FMT));
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
        buildCashFlow();
        buildCategoryDonut();
        buildLargestExpenses();
        buildSavingsGoals();
    }

    // ── Helpers ─────────────────────────────────────────────────────

    private List<Transaction> getMonthTransactions(TransactionType type) {
        Period monthPeriod = new Period(selectedMonth.atDay(1), selectedMonth.atEndOfMonth());
        TransactionConfig config = new TransactionConfig(type, monthPeriod, null, null, currentAccount);
        return transactionManager.getTransactions(config);
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

    // ── Card 1: Cash Flow (Pie Chart) ─────────────────────────────

    private void buildCashFlow() {
        cashFlowChart.getChildren().clear();

        List<Transaction> expenses = getMonthTransactions(TransactionType.EXPENSE);
        List<Transaction> incomes = getMonthTransactions(TransactionType.INCOME);

        double totalExpense = expenses.stream().mapToDouble(t -> (double) t.getAmount()).sum();
        double totalIncome = incomes.stream().mapToDouble(t -> (double) t.getAmount()).sum();

        if (totalExpense == 0 && totalIncome == 0) {
            Label empty = new Label("No transactions this month");
            empty.getStyleClass().add("bar-label");
            cashFlowChart.getChildren().add(empty);
            return;
        }

        PieChart pie = new PieChart();
        pie.setLabelsVisible(true);
        pie.setLegendVisible(false);
        pie.setAnimated(false);
        pie.setPrefSize(180, 180);
        pie.setMaxSize(180, 180);

        if (totalIncome > 0) {
            PieChart.Data incomeSlice = new PieChart.Data("Income", totalIncome);
            incomeSlice.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) newNode.setStyle("-fx-pie-color: #22c55e;");
            });
            pie.getData().add(incomeSlice);
        }

        if (totalExpense > 0) {
            PieChart.Data expenseSlice = new PieChart.Data("Expense", totalExpense);
            expenseSlice.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) newNode.setStyle("-fx-pie-color: #ef4444;");
            });
            pie.getData().add(expenseSlice);
        }

        VBox wrapper = new VBox(pie);
        wrapper.setAlignment(Pos.CENTER);
        cashFlowChart.getChildren().add(wrapper);
    }

    // ── Card 2: Categories Donut ───────────────────────────────────

    private void buildCategoryDonut() {
        donutLegend.getChildren().clear();

        List<Transaction> expenses = getMonthTransactions(TransactionType.EXPENSE);

        Map<String, Double> byCategory = new HashMap<>();
        for (Transaction t : expenses) {
            try {
                String cat = t.getCategory();
                if (cat == null || cat.trim().isEmpty()) continue;
                byCategory.merge(cat.trim(), (double) t.getAmount(), Double::sum);
            } catch (Exception ignored) {
            }
        }

        if (byCategory.isEmpty()) {
            donutTotalLabel.setText("$0");
            Label empty = new Label("No expenses");
            empty.getStyleClass().add("segment-label");
            donutLegend.getChildren().add(empty);
            return;
        }

        double totalSpent = byCategory.values().stream().mapToDouble(d -> d).sum();
        donutTotalLabel.setText(String.format("$%.0f", totalSpent));

        String[] colors = {"#22c55e", "#ef4444", "#f59e0b", "#3b82f6", "#8b5cf6", "#ec4899", "#14b8a6", "#f97316", "#a855f7", "#06b6d4"};

        List<Map.Entry<String, Double>> sorted = byCategory.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        int idx = 0;
        for (Map.Entry<String, Double> entry : sorted) {
            double pct = (entry.getValue() / totalSpent) * 100;
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);

            StackPane dot = new StackPane();
            String clr = colors[idx % colors.length];
            dot.setStyle("-fx-background-color: " + clr + "; -fx-background-radius: 6; -fx-min-width: 10; -fx-min-height: 10; -fx-max-width: 10; -fx-max-height: 10;");

            Label nameLbl = new Label(entry.getKey());
            nameLbl.getStyleClass().add("segment-label");

            StackPane spacer = new StackPane();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label pctLbl = new Label(String.format("%.0f%%", pct));
            pctLbl.getStyleClass().add("segment-percent");

            row.getChildren().addAll(dot, nameLbl, spacer, pctLbl);
            donutLegend.getChildren().add(row);
            idx++;
        }

        if (byCategory.size() > 10) {
            Label more = new Label("+" + (byCategory.size() - 10) + " more");
            more.getStyleClass().add("segment-label");
            more.setStyle("-fx-text-fill: #6B7280; -fx-font-style: italic;");
            donutLegend.getChildren().add(more);
        }
    }

    // ── Card 3: Largest Expenses ───────────────────────────────────

    private void buildLargestExpenses() {
        expenseList.getChildren().clear();

        List<Transaction> allExpenses = getMonthTransactions(TransactionType.EXPENSE);
        List<Transaction> validExpenses = new ArrayList<>();
        for (Transaction t : allExpenses) {
            try {
                if (t.getCategory() != null && t.getAmount() > 0) validExpenses.add(t);
            } catch (Exception ignored) {
            }
        }

        if (validExpenses.isEmpty()) {
            Label empty = new Label("No expenses this month");
            empty.getStyleClass().add("expense-name");
            expenseList.getChildren().add(empty);
            return;
        }

        validExpenses.stream()
                .sorted(Comparator.comparingDouble((Transaction t) -> t.getAmount()).reversed())
                .limit(5)
                .forEach(t -> expenseList.getChildren().add(buildExpenseRow(t)));
    }

    private HBox buildExpenseRow(Transaction t) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("expense-item");

        StackPane iconPane = new StackPane();
        iconPane.getStyleClass().add("expense-icon");
        Circle circle = new Circle(18);
        SVGPath svg = new SVGPath();
        svg.setContent(svgForCategory(t.getCategory()));
        svg.getStyleClass().add("expense-icon-svg");
        iconPane.getChildren().addAll(circle, svg);

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(t.getCategory());
        name.getStyleClass().add("expense-name");
        Label dateLabel = new Label(t.getDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
        dateLabel.getStyleClass().add("expense-category");
        info.getChildren().addAll(name, dateLabel);

        VBox right = new VBox();
        right.setAlignment(Pos.CENTER_RIGHT);
        Label amount = new Label(String.format("-$%.2f", t.getAmount()));
        amount.getStyleClass().add("expense-amount");
        right.getChildren().add(amount);

        row.getChildren().addAll(iconPane, info, right);
        return row;
    }

    // ── Card 4: Savings Goals ──────────────────────────────────────

    private void buildSavingsGoals() {
        goalsList.getChildren().clear();

        List<SavingGoal> goals = state.getGoalsController().getAllSavings(currentAccount);

        if (goals.isEmpty()) {
            Label empty = new Label("No savings goals");
            empty.getStyleClass().add("goal-name");
            goalsList.getChildren().add(empty);
            return;
        }

        for (SavingGoal g : goals) {
            goalsList.getChildren().add(buildGoalRow(g));
        }
    }

    private VBox buildGoalRow(SavingGoal g) {
        VBox box = new VBox(8);

        HBox nameRow = new HBox();
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLbl = new Label(g.getName());
        nameLbl.getStyleClass().add("goal-name");
        nameRow.getChildren().add(nameLbl);

        HBox amtRow = new HBox(8);
        Label current = new Label(String.format("$%.0f", g.getCurrentAmount()));
        current.getStyleClass().add("goal-current");
        Label target = new Label(String.format("/ $%.0f", g.getTargetAmount()));
        target.getStyleClass().add("goal-target");
        amtRow.getChildren().addAll(current, target);

        StackPane progressBg = new StackPane();
        progressBg.getStyleClass().add("goal-progress-bg");

        StackPane progressFill = new StackPane();
        progressFill.getStyleClass().add("goal-progress-fill");
        double pct = g.getTargetAmount() > 0 ? Math.min(g.getCurrentAmount() / g.getTargetAmount(), 1.0) : 0;
        progressFill.setPrefWidth(pct * 200);

        progressBg.getChildren().add(progressFill);

        box.getChildren().addAll(nameRow, amtRow, progressBg);
        return box;
    }

    @FXML
    private void handleAddCategory() {
        DialogHelper.showNewCategoryDialog();
    }

    @FXML
    private void handleExport() {
        if (currentAccount == null) return;
        Period monthPeriod = new Period(selectedMonth.atDay(1), selectedMonth.atEndOfMonth());
        TransactionConfig config = new TransactionConfig(null, monthPeriod, null, null, currentAccount);

        new PDFReport().generate(config);

        String pdfPath = System.getProperty("user.home")
                + File.separator + "Documents"
                + File.separator + "Reports"
                + File.separator + "TransactionReport.pdf";
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Successful");
        alert.setHeaderText(null);
        alert.setContentText("Report saved to:\n" + pdfPath);
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("styles.css").toExternalForm());
        alert.showAndWait();
    }

    @FXML private void handlePrevMonth() {
        selectedMonth = selectedMonth.minusMonths(1);
        monthLabel.setText(selectedMonth.format(MONTH_FMT));
        refresh();
    }

    @FXML private void handleNextMonth() {
        selectedMonth = selectedMonth.plusMonths(1);
        monthLabel.setText(selectedMonth.format(MONTH_FMT));
        refresh();
    }

    @FXML private void handleThisMonth() {
        selectedMonth = YearMonth.now();
        monthLabel.setText(selectedMonth.format(MONTH_FMT));
        refresh();
    }

    @FXML private void navigateToDashboard() { App.showDashboard(); }
    @FXML private void navigateToTransactions() { App.showTransactions(); }
    @FXML private void navigateToBudgets() { App.showBudgets(); }
    @FXML private void navigateToGoals() { App.showGoals(); }
    @FXML private void navigateToReports() { App.showReports(); }
    @FXML private void navigateToProfile() { App.showProfile(); }
}
