package com.musomi.desktop.util;

import java.net.URL;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import com.musomi.desktop.config.SceneManager;

/**
 * Utility for displaying non-blocking toast notifications in the desktop application.
 *
 * <p>Toasts appear as lightweight, transparent stages positioned at the top-right
 * of the primary application window and automatically dismiss after 3 seconds.
 */
public final class Toast {

    private static final String CSS_COMPONENTS = "/css/components.css";
    private static final Duration DISPLAY_DURATION = Duration.seconds(3);
    private static final double OFFSET_X = 24.0;
    private static final double OFFSET_Y = 24.0;
    private static final double DEFAULT_TOAST_WIDTH = 250.0;

    private Toast() {}

    /**
     * Displays a success toast notification.
     *
     * @param message notification text to display
     */
    public static void success(String message) {
        show(message, "toast-success");
    }

    /**
     * Displays an error toast notification.
     *
     * @param message notification text to display
     */
    public static void error(String message) {
        show(message, "toast-error");
    }

    /**
     * Displays an informational toast notification.
     *
     * @param message notification text to display
     */
    public static void info(String message) {
        show(message, "toast-info");
    }

    /**
     * Displays a warning toast notification.
     *
     * @param message notification text to display
     */
    public static void warning(String message) {
        show(message, "toast-warning");
    }

    private static void show(String message, String styleClass) {
        Platform.runLater(() -> {
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initModality(Modality.NONE);
            stage.setAlwaysOnTop(true);

            Label label = new Label(message);
            label.getStyleClass().addAll("toast", styleClass);

            Scene scene = new Scene(new StackPane(label));
            scene.setFill(Color.TRANSPARENT);

            URL cssResource = Toast.class.getResource(CSS_COMPONENTS);
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            }

            stage.setScene(scene);

            stage.setOnShown(e -> positionTopRight(stage));
            stage.show();
            positionTopRight(stage);

            PauseTransition delay = new PauseTransition(DISPLAY_DURATION);
            delay.setOnFinished(e -> stage.close());
            delay.play();
        });
    }

    private static void positionTopRight(Stage stage) {
        Window owner = getPrimaryWindow();
        if (owner != null && owner.isShowing()) {
            double width = resolveStageWidth(stage);
            stage.setX(owner.getX() + owner.getWidth() - width - OFFSET_X);
            stage.setY(owner.getY() + OFFSET_Y);
        } else {
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            double width = resolveStageWidth(stage);
            stage.setX(bounds.getMaxX() - width - OFFSET_X);
            stage.setY(bounds.getMinY() + OFFSET_Y);
        }
    }

    private static double resolveStageWidth(Stage stage) {
        double width = stage.getWidth();
        if (Double.isNaN(width) || width <= 0) {
            if (stage.getScene() != null && stage.getScene().getRoot() != null) {
                width = stage.getScene().getRoot().prefWidth(-1);
            }
        }
        if (Double.isNaN(width) || width <= 0) {
            width = DEFAULT_TOAST_WIDTH;
        }
        return width;
    }

    private static Window getPrimaryWindow() {
        try {
            Stage primary = SceneManager.getInstance().getPrimaryStage();
            if (primary != null && primary.isShowing()) {
                return primary;
            }
        } catch (Exception ignored) {
            // Fall back to scanning active windows
        }

        for (Window window : Window.getWindows()) {
            if (window.isShowing() && window instanceof Stage) {
                return window;
            }
        }
        return null;
    }
}
