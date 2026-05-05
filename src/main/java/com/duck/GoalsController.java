package com.duck;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import java.io.IOException;

public class GoalsController {

    @FXML
    private FlowPane cardsContainer;

    @FXML
    public void initialize() {
        System.out.println("Goals page loaded");
        addDummyData();
    }

    @FXML
    private void navigateToDashboard() {
        App.showDashboard();
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
        System.out.println("Already on Goals");
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
    private void handleAddTransaction() {
        System.out.println("Add Transaction clicked");
    }

    public void addGoalCard(String category, double spent, double limit, int daysRemaining) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("goal_card.fxml"));
            AnchorPane card = loader.load();
            
            GoalCardController controller = loader.getController();
            controller.setCardData(category, spent, limit, daysRemaining);
            
            cardsContainer.getChildren().add(card);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void addDummyData() {
        addGoalCard("GROCERIES & DINING", 850, 1000, 12);
        addGoalCard("TRANSPORTATION", 120, 400, 12);
        addGoalCard("ENTERTAINMENT", 290, 300, 12);
    }
}
