package com.duck;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class ReportsController implements Initializable {

    // ── FXML injections ────────────────────────────────────────────
    @FXML private HBox  cashFlowChart;
    @FXML private Label donutTotalLabel;
    @FXML private VBox  donutLegend;
    @FXML private VBox  expenseList;
    @FXML private VBox  goalsList;

    // ── SVG paths ──────────────────────────────────────────────────
    private static final String SVG_HOUSE = "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z";
    private static final String SVG_CAR   = "M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z";
    private static final String SVG_FOOD  = "M11 9H9V2H7v7H5V2H3v7c0 2.12 1.66 3.84 3.75 3.97V22h2.5v-9.03C11.34 12.84 13 11.12 13 9V2h-2v7zm5-3v8h2.5v8H21V2c-2.76 0-5 2.24-5 4z";

    // ── Data models ────────────────────────────────────────────────

    /** One bar in the Cash Flow chart */
    public static class DayFlow {
        public String  label;
        public double  income;
        public double  expense;
        public DayFlow(String label, double income, double expense) {
            this.label = label; this.income = income; this.expense = expense;
        }
    }

    /** One slice in the Categories donut */
    public static class CategorySlice {
        public String name;
        public double percent;
        public String colorStyle;   // CSS style class for the legend dot
        public CategorySlice(String name, double percent, String colorStyle) {
            this.name = name; this.percent = percent; this.colorStyle = colorStyle;
        }
    }

    /** One row in Largest Expenses */
    public static class ExpenseItem {
        public String  name;
        public String  category;
        public double  amount;      // always positive
        public boolean recurring;
        public String  svgPath;
        public ExpenseItem(String name, String category, double amount,
                           boolean recurring, String svgPath) {
            this.name = name; this.category = category; this.amount = amount;
            this.recurring = recurring; this.svgPath = svgPath;
        }
    }

    /** One row in Savings Goals */
    public static class SavingsGoal {
        public String name;
        public double current;
        public double target;
        public SavingsGoal(String name, double current, double target) {
            this.name = name; this.current = current; this.target = target;
        }
        public double percent() { return target > 0 ? current / target : 0; }
    }

    // ── Data lists ─────────────────────────────────────────────────
    private final List<DayFlow>      cashFlowData  = new ArrayList<>();
    private final List<CategorySlice> categoryData = new ArrayList<>();
    private final List<ExpenseItem>   expenseData  = new ArrayList<>();
    private final List<SavingsGoal>   goalData     = new ArrayList<>();

    // ── Lifecycle ──────────────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Cash Flow — seed (income, expense) heights per day
        cashFlowData.add(new DayFlow("Mon",  60,   0));
        cashFlowData.add(new DayFlow("Tue", 100,   0));
        cashFlowData.add(new DayFlow("Wed",  50,  40));
        cashFlowData.add(new DayFlow("Thu", 120,   0));
        cashFlowData.add(new DayFlow("Fri",  70,   0));
        cashFlowData.add(new DayFlow("Sat",  30,  90));
        cashFlowData.add(new DayFlow("Sun",  65,   0));

        // Categories
        categoryData.add(new CategorySlice("Housing",        40, "legend-segment-green"));
        categoryData.add(new CategorySlice("Food & Dining",  35, "legend-segment-red"));
        categoryData.add(new CategorySlice("Transportation", 25, "legend-segment-gray"));

        // Largest Expenses
        expenseData.add(new ExpenseItem("Monthly Rent",   "Housing",        1800.00, true,  SVG_HOUSE));
        expenseData.add(new ExpenseItem("Auto Insurance", "Transportation",  245.50, false, SVG_CAR));

        // Savings Goals
        goalData.add(new SavingsGoal("Emergency Fund",  8500, 10000));
        goalData.add(new SavingsGoal("Vacation 2024",   1200,  3000));

        render();
    }

    // ── FXML handlers ──────────────────────────────────────────────

    @FXML
    private void handleAddTransaction() {
        // Demo: add a sample expense and refresh
        expenseData.add(new ExpenseItem("New Expense", "Other", 50.00, false, SVG_FOOD));
        renderExpenses();
    }

    @FXML
    private void handleExport() {
        // TODO: implement CSV / PDF export
        System.out.println("Export clicked");
    }

    // ── Master render ──────────────────────────────────────────────

    private void render() {
        renderCashFlow();
        renderDonut();
        renderExpenses();
        renderGoals();
    }

    // ── Card 1: Cash Flow ──────────────────────────────────────────

    private void renderCashFlow() {
        cashFlowChart.getChildren().clear();

        for (DayFlow day : cashFlowData) {
            VBox col = new VBox(4);
            col.setAlignment(Pos.BOTTOM_CENTER);

            if (day.expense > 0 && day.income > 0) {
                // Stacked: expense on top, income below
                VBox stack = new VBox(2);
                stack.setAlignment(Pos.BOTTOM_CENTER);
                stack.getChildren().addAll(
                        bar("bar-expense", 25, (int) day.expense),
                        bar("bar-income",  25, (int) day.income)
                );
                col.getChildren().add(stack);
            } else if (day.income > 0) {
                col.getChildren().add(bar("bar-income", 30, (int) day.income));
            } else {
                col.getChildren().add(bar("bar-expense", 30, (int) day.expense));
            }

            Label lbl = new Label(day.label);
            lbl.getStyleClass().add("bar-label");
            col.getChildren().add(lbl);

            cashFlowChart.getChildren().add(col);
        }
    }

    /** Creates a single bar StackPane with fixed width and variable height */
    private StackPane bar(String styleClass, int width, int height) {
        StackPane b = new StackPane();
        b.getStyleClass().add(styleClass);
        b.setMinWidth(width);  b.setMaxWidth(width);
        b.setMinHeight(height); b.setMaxHeight(height);
        return b;
    }

    // ── Card 2: Donut legend ───────────────────────────────────────

    private void renderDonut() {
        donutLegend.getChildren().clear();

        double total = categoryData.stream().mapToDouble(s -> s.percent).sum();
        // Rough dollar total: reuse expenseData sum as reference
        double spent = expenseData.stream().mapToDouble(e -> e.amount).sum();
        donutTotalLabel.setText(String.format("$%.1fk", spent / 1000.0));

        for (CategorySlice slice : categoryData) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);

            StackPane dot = new StackPane();
            dot.getStyleClass().add(slice.colorStyle);

            Label nameLbl = new Label(slice.name);
            nameLbl.getStyleClass().add("segment-label");

            StackPane spacer = new StackPane();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label pctLbl = new Label(String.format("%.0f%%", slice.percent));
            pctLbl.getStyleClass().add("segment-percent");

            row.getChildren().addAll(dot, nameLbl, spacer, pctLbl);
            donutLegend.getChildren().add(row);
        }
    }

    // ── Card 3: Largest Expenses ───────────────────────────────────

    private void renderExpenses() {
        expenseList.getChildren().clear();

        // Show top 5 by amount descending
        expenseData.stream()
                .sorted(Comparator.comparingDouble((ExpenseItem e) -> e.amount).reversed())
                .limit(5)
                .forEach(e -> expenseList.getChildren().add(buildExpenseRow(e)));
    }

    private HBox buildExpenseRow(ExpenseItem e) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("expense-item");

        // Icon
        StackPane iconPane = new StackPane();
        iconPane.getStyleClass().add("expense-icon");
        Circle circle = new Circle(18);
        circle.setFill(Color.web("#1F2937"));
        SVGPath svg = new SVGPath();
        svg.setContent(e.svgPath);
        svg.getStyleClass().add("expense-icon-svg");
        iconPane.getChildren().addAll(circle, svg);

        // Name + category
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(e.name);
        name.getStyleClass().add("expense-name");
        Label cat = new Label(e.category);
        cat.getStyleClass().add("expense-category");
        info.getChildren().addAll(name, cat);

        // Amount (+ optional badge)
        VBox right = new VBox();
        right.setAlignment(Pos.CENTER_RIGHT);
        Label amount = new Label(String.format("-$%.2f", e.amount));
        amount.getStyleClass().add("expense-amount");
        right.getChildren().add(amount);
        if (e.recurring) {
            Label badge = new Label("Recurring");
            badge.getStyleClass().add("recurring-badge");
            right.getChildren().add(badge);
        }

        row.getChildren().addAll(iconPane, info, right);
        return row;
    }

    // ── Card 4: Savings Goals ──────────────────────────────────────

    private void renderGoals() {
        goalsList.getChildren().clear();
        for (SavingsGoal g : goalData) {
            goalsList.getChildren().add(buildGoalRow(g));
        }
    }

    private VBox buildGoalRow(SavingsGoal g) {
        VBox box = new VBox(8);

        // Goal name
        HBox nameRow = new HBox();
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLbl = new Label(g.name);
        nameLbl.getStyleClass().add("goal-name");
        nameRow.getChildren().add(nameLbl);

        // Current / target amounts
        HBox amtRow = new HBox(8);
        Label current = new Label(String.format("$%.0f", g.current));
        current.getStyleClass().add("goal-current");
        Label target = new Label(String.format("/ $%.0f", g.target));
        target.getStyleClass().add("goal-target");
        amtRow.getChildren().addAll(current, target);

        // Progress bar
        StackPane progressBg = new StackPane();
        progressBg.getStyleClass().add("goal-progress-bg");

        StackPane progressFill = new StackPane();
        progressFill.getStyleClass().add("goal-progress-fill");
        // Width proportional to percentage (max ~200 px reference)
        double fillWidth = Math.min(g.percent(), 1.0) * 200;
        progressFill.setPrefWidth(fillWidth);

        progressBg.getChildren().add(progressFill);

        box.getChildren().addAll(nameRow, amtRow, progressBg);
        return box;
    }
}
