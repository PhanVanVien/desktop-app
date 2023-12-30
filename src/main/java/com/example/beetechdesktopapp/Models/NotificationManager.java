package com.example.beetechdesktopapp.Models;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.controlsfx.control.Notifications;
import javafx.util.Duration;

public class NotificationManager {
    private static NotificationManager instance;

    private NotificationManager() {
        // Private constructor to enforce singleton pattern
    }

    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    public void showNotification(String title, String text, String imageName) {
        Image image = new Image(getClass().getResource("/Images/" + imageName).toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(50);
        imageView.setFitHeight(50);
        Notifications notifications = Notifications.create();
        notifications.graphic(imageView);
        notifications.text(text);
        notifications.title(title);
        notifications.hideAfter(Duration.seconds(1));
        notifications.position(Pos.BASELINE_RIGHT);
        notifications.show();
    }
}
