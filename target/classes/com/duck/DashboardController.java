package com.duck;

import com.duck.model.authentication.Session;
import com.duck.model.records.Budget;
import com.duck.model.records.BudgetController;
import com.duck.model.records.SavingGoalsController;
import com.duck.model.records.TransactionManager;
import com.duck.model.dataAccessors.LocalStorage;
import com.duck.model.type.Account;
import com.duck.model.type.Period;
import com.duck.model.type.SavingGoal;
import com.duck.model.type.Transaction;
import com.duck.model.type.TransactionConfig;
import com.duck.model.type.AppSettings;
import com.duck.model.type.AppSettings.Currency;
import com.duck.model.type.AppSettings.TransactionType;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import javafx.scene.layout.Priority;

public class DashboardController implements PropertyChangeListener {

    @FXML private StackPane sidebarAvatarContainer;
    @FXML private Label balanceLabel;
    @FXML private Label balanceBadgeLabel;
    @FXML private Label spendingAmountLabel;
    @FXML private Label spendingLimitLabel;
    @FXML private Label spendingInfoLabel;
    @FXML private Label incomeAmountLabel;
    @FXML private Label daysLeftLabel;
    @FXML private StackPane progressBarFill;
    @FXML private VBox goalsContainer;

    private final TransactionManager transactionManager;
    private final BudgetController budgetController;
    private final SavingGoalsController goalsController;
    private final ApplicationState state;

    private Account currentAccount;

    public DashboardController() {
        this.state = ApplicationState.getInstance();
        this.transactionManager = state.getTransactionManager();
        this.budgetController = state.getBudgetController();
        this.goalsController = state.getGoalsController();
    }

    @FXML
    public void initialize() {
        transactionManager.addPropertyChangeListener(this);
        budgetController.addPropertyChangeListener(this);
        goalsController.addPropertyChangeListener(this);

        loadCurrentAccount();
        applyTheme();
        refreshDashboard();
    }

    private void applyTheme() {
        if (currentAccount == null) return;
        AppSettings.Mode mode = AppSettings.Mode.DARK;
        if (currentAccount.getAccountConfig() != null && currentAccount.getAccountConfig().getMode() != null) {
            mode = currentAccount.getAccountConfig().getMode();
        }
        App.setTheme(mode);
    }

    private void loadCurrentAccount() {
        String token = Session.getInstance().getToken();
        if (token == null) return;

        String email = extractEmailFromToken(token);
        if (email == null) return;

        List<Account> accounts = state.getStorage().getAccounts();
        if (accounts != null) {
            for (Account acc : accounts) {
                if (acc.getEmail().equalsIgnoreCase(email)) {
                    currentAccount = acc;
                    break;
                }
            }
        }
        AvatarHelper.setSidebarAvatar(sidebarAvatarContainer, currentAccount);
    }

    private String extractEmailFromToken(String token) {
        try {
            String[] parts = token.split("_");
            return (parts.length >= 2) ? parts[1] : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String currencySymbol() {
        return CurrencyUtil.getSymbol(getCurrency());
    }

    private void refreshDashboard() {
        String sym = currencySymbol();
        if (balanceLabel != null) balanceLabel.setText(sym + "0.00");
        if (balanceBadgeLabel != null) balanceBadgeLabel.setText("0.0%");
        if (spendingAmountLabel != null) spendingAmountLabel.setText(sym + "0");
        if (spendingLimitLabel != null) spendingLimitLabel.setText("/ " + sym + "0");
        if (incomeAmountLabel != null) { incomeAmountLabel.setText(sym + "0 earned"); incomeAmountLabel.setStyle("-fx-text-fill: #6B7280;"); }
        if (spendingInfoLabel != null) spendingInfoLabel.setText("No budgets set");
        if (daysLeftLabel != null) daysLeftLabel.setText("");
        if (progressBarFill != null) progressBarFill.setMaxWidth(0);
        if (goalsContainer != null) goalsContainer.getChildren().clear();

        if (currentAccount == null) return;

        loadBalance();
        loadMonthlySpending();
        loadGoals();
    }

    private Currency getCurrency() {
        if (currentAccount != null && currentAccount.getAccountConfig() != null
                && currentAccount.getAccountConfig().getCurrency() != null) {
            return currentAccount.getAccountConfig().getCurrency();
        }
        return Currency.USD;
    }

    private void loadBalance() {
        TransactionConfig config = new TransactionConfig(null, null, null, null, currentAccount);
        List<Transaction> allTransactions = transactionManager.getTransactions(config);
        float balance = currentAccount.getBalance();
        YearMonth currentMonth = YearMonth.now();

        float monthIncome = 0;
        float monthExpense = 0;

        for (Transaction t : allTransactions) {
            if (t.getType() == TransactionType.INCOME) {
                balance += t.getAmount();
            } else {
                balance -= t.getAmount();
            }

            if (YearMonth.from(t.getDate()).equals(currentMonth)) {
                if (t.getType() == TransactionType.INCOME) {
                    monthIncome += t.getAmount();
                } else {
                    monthExpense += t.getAmount();
                }
            }
        }

        if (balanceLabel != null) {
            balanceLabel.setText(CurrencyUtil.format(balance, getCurrency()));
        }

        float netFlow = monthIncome - monthExpense;
        if (balanceBadgeLabel != null) {
            if (monthIncome > 0) {
                float percent = (netFlow / monthIncome) * 100;
                String sign = netFlow >= 0 ? "+" : "";
                balanceBadgeLabel.setText(String.format("%s%.1f%%", sign, percent));
            } else {
                balanceBadgeLabel.setText("0.0%");
            }
        }
    }

    private void loadMonthlySpending() {
        YearMonth currentMonth = YearMonth.now();
        LocalDate start = currentMonth.atDay(1);
        LocalDate end = currentMonth.atEndOfMonth();
        Period monthPeriod = new Period(start, end);

        float totalExpense = 0;
        float totalIncome = 0;
        float totalBudget = 0;

        TransactionConfig expenseConfig = new TransactionConfig(TransactionType.EXPENSE, monthPeriod, null, null, currentAccount);
        for (Transaction t : transactionManager.getTransactions(expenseConfig)) {
            totalExpense += t.getAmount();
        }

        TransactionConfig incomeConfig = new TransactionConfig(TransactionType.INCOME, monthPeriod, null, null, currentAccount);
        for (Transaction t : transactionManager.getTransactions(incomeConfig)) {
            totalIncome += t.getAmount();
        }

        List<Budget> budgets = budgetController.getAllBudgets(currentAccount);
        for (Budget b : budgets) {
            if (b.isActive()) {
                totalBudget += b.getAmount();
            }
        }

        String sym = currencySymbol();
        if (spendingAmountLabel != null) spendingAmountLabel.setText(String.format(sym + "%,.0f spent", totalExpense));
        if (spendingLimitLabel != null) spendingLimitLabel.setText(String.format("/ " + sym + "%,.0f budget", totalBudget));

        if (incomeAmountLabel != null) {
            incomeAmountLabel.setText(String.format(sym + "%,.0f earned", totalIncome));
            incomeAmountLabel.setStyle(totalIncome > 0 ? "-fx-text-fill: #22c55e;" : "-fx-text-fill: #6B7280;");
        }

        if (totalBudget > 0) {
            int percent = (int) ((totalExpense / totalBudget) * 100);
            long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), end);
            if (spendingInfoLabel != null) spendingInfoLabel.setText(percent + "% of budget");
            if (daysLeftLabel != null) daysLeftLabel.setText(daysRemaining + " days left");
            if (progressBarFill != null) {
                double pct = Math.min(totalExpense / totalBudget, 1.0);
                progressBarFill.setMaxWidth(pct * 600);
            }
        } else {
            if (spendingInfoLabel != null) spendingInfoLabel.setText("No budgets set");
            if (daysLeftLabel != null) daysLeftLabel.setText("");
            if (progressBarFill != null) progressBarFill.setMaxWidth(0);
        }
    }

    private void loadGoals() {
        if (goalsContainer == null) return;

        goalsContainer.getChildren().clear();
        List<SavingGoal> goals = goalsController.getAllSavings(currentAccount);

        boolean hasActive = false;
        for (SavingGoal goal : goals) {
            if (goal.isActive()) {
                HBox goalCard = createGoalCard(goal);
                goalsContainer.getChildren().add(goalCard);
                hasActive = true;
            }
        }

        if (!hasActive) {
            Label noGoalsLabel = new Label("No active saving goals");
            noGoalsLabel.getStyleClass().add("page-subtitle");
            goalsContainer.getChildren().add(noGoalsLabel);
        }
    }

    private HBox createGoalCard(SavingGoal goal) {
        HBox card = new HBox(16);
        card.getStyleClass().add("goal-card");

        double percentage = goal.getTargetAmount() > 0
            ? (goal.getCurrentAmount() / goal.getTargetAmount()) * 100
            : 0;

        VBox textGroup = new VBox(4);
        HBox.setHgrow(textGroup, Priority.ALWAYS);

        Label titleLabel = new Label(goal.getName());
        titleLabel.getStyleClass().add("goal-title");

        String sym = currencySymbol();
        Label amountLabel = new Label(String.format(sym + "%,.0f / " + sym + "%,.0f", goal.getCurrentAmount(), goal.getTargetAmount()));
        amountLabel.getStyleClass().add("goal-amount");

        textGroup.getChildren().addAll(titleLabel, amountLabel);

        Label percentLabel = new Label(String.format("%.0f%%", percentage));
        percentLabel.getStyleClass().add("progress-percent");

        long daysRemaining = goal.getDeadline() != null
            ? ChronoUnit.DAYS.between(LocalDate.now(), goal.getDeadline())
            : 0;
        Label daysLabel = new Label(daysRemaining + " days left");
        daysLabel.getStyleClass().add("goal-days-left");

        VBox rightGroup = new VBox(4);
        rightGroup.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        rightGroup.getChildren().addAll(percentLabel, daysLabel);

        card.getChildren().addAll(textGroup, rightGroup);
        return card;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        refreshDashboard();
    }

    @FXML
    private void navigateToTransactions() {
        App.showTransactions();
    }

    @FXML
    private void navigateToBudgets() {
        App.showBudgets();
    }

    @FXML
    private void navigateToGoals() {
        App.showGoals();
    }

    @FXML
    private void navigateToReports() {
        App.showReports();
    }

    @FXML
    private void navigateToProfile() {
        App.showProfile();
    }

    @FXML
    private void handleAddCategory() {
        DialogHelper.showNewCategoryDialog();
    }

    @FXML
    private void handleDeposit() {
        Transaction tx = DialogHelper.showTransactionDialog(currentAccount, TransactionType.INCOME);
        if (tx != null) {
            transactionManager.addTransaction(tx);
            refreshDashboard();
        }
    }

    @FXML
    private void handleTransfer() {
        Transaction tx = DialogHelper.showTransactionDialog(currentAccount, TransactionType.EXPENSE);
        if (tx != null) {
            transactionManager.addTransaction(tx);
            refreshDashboard();
        }
    }
}
