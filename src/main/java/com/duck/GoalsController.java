package com.duck;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class GoalsController implements Initializable {

    @FXML private FlowPane cardsContainer;

    // ── Goal data model ────────────────────────────────────────────
    public static class Goal {
        public String    category;       // e.g. "Food", "Housing"
        public double    spent;
        public double    limit;
        public int       daysRemaining;
        public LocalDate cycleEnd;

        public Goal(String category, double spent, double limit, int daysRemaining) {
            this.category      = category;
            this.spent         = spent;
            this.limit         = limit;
            this.daysRemaining = daysRemaining;
        }

        public double percent()  { return limit > 0 ? spent / limit : 0; }
        public boolean isOver()  { return spent >  limit; }
        public boolean isWarn()  { return !isOver() && percent() >= 0.75; }

        /** "On Track" | "Warning" | "Over Budget" */
        public String statusText() {
            if (isOver()) return "Over Budget";
            if (isWarn()) return "Warning";
            return "On Track";
        }

        /** CSS style class applied to the left border Region */
        public String borderStyle() {
            if (isOver()) return "status-border-red";
            if (isWarn()) return "status-border-yellow";
            return "status-border-green";
        }
    }

    private final List<Goal> goals = new ArrayList<>();

    // ── Lifecycle ──────────────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Seed data — matches the hard-coded cards in the original design
        goals.add(new Goal("Food",           280,  400,  18));
        goals.add(new Goal("Housing",        450,  600,  18));
        goals.add(new Goal("Transportation", 180,  250,  18));
        goals.add(new Goal("Utilities",      150,  200,  18));
        goals.add(new Goal("Entertainment",  120,  150,  18));
        goals.add(new Goal("Shopping",       200,  150,  18));  // over budget

        renderCards();
    }

    // ── FXML handlers ──────────────────────────────────────────────

    @FXML
    private void handleNewBudget() {
        // TODO: open a dialog to collect category / limit, then add to list
        // Demo: add a placeholder card
        goals.add(new Goal("New Category", 0, 100, 30));
        renderCards();
    }

    @FXML
    private void handleFilter() {
        // TODO: implement filter dialog / toggle
        System.out.println("Filter clicked");
    }

    // ── Rendering ──────────────────────────────────────────────────

    private void renderCards() {
        cardsContainer.getChildren().clear();

        for (Goal goal : goals) {
            try {
                URL fxmlUrl = getClass().getResource("/com/duck/goal_card.fxml");
                if (fxmlUrl == null) {
                    throw new IllegalStateException("goal_card.fxml resource not found: /com/duck/goal_card.fxml");
                }

                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Node card = loader.load();


                // Pass data to the card's own controller
                GoalCardController cardCtrl = loader.getController();
                cardCtrl.setGoal(goal);

                cardsContainer.getChildren().add(card);

            } catch (IOException e) {
                throw new RuntimeException("Failed to load goal card FXML", e);
            }

        }
    }
}
