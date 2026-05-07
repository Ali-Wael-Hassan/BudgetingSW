package com.duck;

import com.duck.model.type.Account;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

import java.io.File;

public class AvatarHelper {

    private static final double SIDEBAR_SIZE = 48;
    private static final double SIDEBAR_RADIUS = 24;

    public static void setSidebarAvatar(StackPane container, Account account) {
        if (container == null) return;
        container.getChildren().clear();

        String avatarPath = account != null && account.getAccountConfig() != null
                ? account.getAccountConfig().getAvatarPath() : null;

        if (avatarPath != null && !avatarPath.isEmpty()) {
            try {
                Image img = new Image(new File(avatarPath).toURI().toString());
                ImageView view = new ImageView(img);
                view.setFitWidth(SIDEBAR_SIZE);
                view.setFitHeight(SIDEBAR_SIZE);
                view.setPreserveRatio(false);
                Circle clip = new Circle(SIDEBAR_RADIUS, SIDEBAR_RADIUS, SIDEBAR_RADIUS);
                view.setClip(clip);
                container.getChildren().add(view);
                return;
            } catch (Exception e) {
                System.err.println("Failed to load sidebar avatar: " + e.getMessage());
            }
        }

        Circle circle = new Circle(SIDEBAR_RADIUS);
        circle.setFill(javafx.scene.paint.Color.web("#1F2937"));
        SVGPath svg = new SVGPath();
        svg.setContent("M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z");
        svg.setFill(javafx.scene.paint.Color.web("#6B7280"));
        container.getChildren().addAll(circle, svg);
    }
}
