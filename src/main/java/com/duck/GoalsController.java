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
        // Page starts empty as requested.
        // Calling dummy data here just so you can see the design in action:
        addDummyData();
    }

    /**
     * Call this method to dynamically add a new Goal Card to the view.
     */
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
    
    // Example usage for testing:
    public void addDummyData() {
        addGoalCard("GROCERIES & DINING", 850, 1000, 12);
        addGoalCard("TRANSPORTATION", 120, 400, 12);
        addGoalCard("ENTERTAINMENT", 290, 300, 12);
    }
}
