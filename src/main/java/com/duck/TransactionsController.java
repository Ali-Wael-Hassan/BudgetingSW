package com.duck;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TransactionsController implements Initializable {

    @FXML
    private VBox transactionsContainer;

    // SVG paths reused per category
    private static final String SVG_SHOPPING  = "M7 18c-1.1 0-1.99.9-1.99 2S5.9 22 7 22s2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42c-.14 0-.25-.11-.25-.25l.03-.12.9-1.63h7.45c.75 0 1.41-.41 1.75-1.03l3.58-6.49c.08-.14.12-.31.12-.48 0-.55-.45-1-1-1H5.21l-.94-2H1zm16 16c-1.1 0-1.99.9-1.99 2s.89 2 1.99 2 2-.9 2-2-.9-2-2-2z";
    private static final String SVG_MONEY     = "M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.35 3.68 3.74V19h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z";
    private static final String SVG_FOOD      = "M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z";
    private static final String SVG_CAR       = "M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z";
    private static final String SVG_UTILITIES = "M20 4H4v2h16V4zm1 10v-2l-1-5H4l-1 5v2h1v6h10v-6h4v6h2v-6h1zm-9 4H6v-4h6v4z";

    // Simple Transaction model
    public static class Transaction {
        public String name;
        public String category;
        public double amount;  // positive = income, negative = expense
        public String dateGroup; // e.g. "Today", "Yesterday", "This Week"
        public String svgPath;

        public Transaction(String name, String category, double amount, String dateGroup, String svgPath) {
            this.name = name;
            this.category = category;
            this.amount = amount;
            this.dateGroup = dateGroup;
            this.svgPath = svgPath;
        }
    }

    private List<Transaction> transactions = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Seed with the original hard-coded data
        transactions.add(new Transaction("Grocery Store",     "Food",           -85.50, "Today",     SVG_SHOPPING));
        transactions.add(new Transaction("Salary",            "Income",        3200.00, "Today",     SVG_MONEY));
        transactions.add(new Transaction("Coffee Shop",       "Food",            -5.50, "Yesterday", SVG_FOOD));
        transactions.add(new Transaction("Gas Station",       "Transportation", -45.00, "Yesterday", SVG_CAR));
        transactions.add(new Transaction("Freelance Payment", "Income",         500.00, "This Week", SVG_MONEY));
        transactions.add(new Transaction("Internet Bill",     "Utilities",      -75.00, "This Week", SVG_UTILITIES));

        renderTransactions();
    }

    // Called when "+" Add Transaction button is pressed
    @FXML
    private void handleAddTransaction() {
        // TODO: replace this with a real dialog/form
        // For now, adds a sample transaction to demonstrate dynamic behaviour
        transactions.add(new Transaction(
                "New Transaction",
                "Other",
                -10.00,
                "Today",
                SVG_SHOPPING
        ));
        renderTransactions();
    }

    // ---------------------------------------------------------------
    // Rendering helpers
    // ---------------------------------------------------------------

    private void renderTransactions() {
        transactionsContainer.getChildren().clear();

        // Group transactions by dateGroup, preserving insertion order
        List<String> seenGroups = new ArrayList<>();
        for (Transaction t : transactions) {
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

            for (Transaction t : transactions) {
                if (t.dateGroup.equals(group)) {
                    groupBox.getChildren().add(buildTransactionRow(t));
                }
            }

            transactionsContainer.getChildren().add(groupBox);
        }
    }

    private HBox buildTransactionRow(Transaction t) {
        HBox row = new HBox(12);
        row.getStyleClass().add("transaction-row");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Icon stack
        javafx.scene.layout.StackPane iconPane = new javafx.scene.layout.StackPane();
        iconPane.getStyleClass().add("transaction-icon");
        Circle circle = new Circle(20);
        circle.setFill(javafx.scene.paint.Color.web("#1F2937"));
        SVGPath svg = new SVGPath();
        svg.setContent(t.svgPath);
        svg.getStyleClass().add("transaction-icon-svg");
        iconPane.getChildren().addAll(circle, svg);

        // Name + category
        VBox info = new VBox(2);
        HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);
        Label name = new Label(t.name);
        name.getStyleClass().add("transaction-name");
        Label category = new Label(t.category);
        category.getStyleClass().add("transaction-category");
        info.getChildren().addAll(name, category);

        // Amount
        boolean isIncome = t.amount >= 0;
        Label amountLabel = new Label(
                (isIncome ? "+" : "") + String.format("$%.2f", t.amount)
        );
        amountLabel.getStyleClass().add(isIncome ? "amount-income" : "amount-expense");

        row.getChildren().addAll(iconPane, info, amountLabel);
        return row;
    }
}
