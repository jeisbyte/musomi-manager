package com.musomi.desktop.util;

import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.Window;

import com.musomi.desktop.config.SceneManager;

/**
 * Utility for displaying modal dialogs and alerts in the desktop application.
 *
 * <p>Wraps standard JavaFX {@link Alert} dialogs with consistent titles, styling,
 * owner stage attachment, and blocking behavior.
 */
public final class Modal {

    private static final String CSS_STYLES = "/css/styles.css";
    private static final String CSS_COMPONENTS = "/css/components.css";

    private Modal() {}

    /**
     * Displays a confirmation dialog and blocks until the user responds.
     *
     * @param message the confirmation prompt to display
     * @return {@code true} if the user confirms with OK; {@code false} if cancelled or dismissed
     */
    public static boolean confirm(String message) {
        if (!Platform.isFxApplicationThread()) {
            FutureTask<Boolean> task = new FutureTask<>(() -> showConfirmDialog(message));
            Platform.runLater(task);
            try {
                return task.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } catch (ExecutionException e) {
                return false;
            }
        }
        return showConfirmDialog(message);
    }

    /**
     * Displays an error alert dialog and blocks until the user closes it.
     *
     * @param message the error message to display
     */
    public static void error(String message) {
        if (!Platform.isFxApplicationThread()) {
            FutureTask<Void> task = new FutureTask<>(() -> {
                showAlert(AlertType.ERROR, "Error", message);
                return null;
            });
            Platform.runLater(task);
            try {
                task.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException ignored) {
                // Dialog execution completed
            }
            return;
        }
        showAlert(AlertType.ERROR, "Error", message);
    }

    /**
     * Displays an informational alert dialog and blocks until the user closes it.
     *
     * @param message the informational message to display
     */
    public static void info(String message) {
        if (!Platform.isFxApplicationThread()) {
            FutureTask<Void> task = new FutureTask<>(() -> {
                showAlert(AlertType.INFORMATION, "Info", message);
                return null;
            });
            Platform.runLater(task);
            try {
                task.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException ignored) {
                // Dialog execution completed
            }
            return;
        }
        showAlert(AlertType.INFORMATION, "Info", message);
    }

    private static boolean showConfirmDialog(String message) {
        Alert alert = createAlert(AlertType.CONFIRMATION, "Confirm", message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void showAlert(AlertType alertType, String title, String message) {
        Alert alert = createAlert(alertType, title, message);
        alert.showAndWait();
    }

    private static Alert createAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        attachOwner(alert);
        applyStylesheets(alert);

        return alert;
    }

    private static void attachOwner(Alert alert) {
        try {
            Stage primary = SceneManager.getInstance().getPrimaryStage();
            if (primary != null && primary.isShowing()) {
                alert.initOwner(primary);
                return;
            }
        } catch (Exception ignored) {
            // SceneManager might not be initialized
        }

        for (Window window : Window.getWindows()) {
            if (window.isShowing() && window instanceof Stage stage) {
                alert.initOwner(stage);
                return;
            }
        }
    }

    private static void applyStylesheets(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        if (dialogPane == null) {
            return;
        }

        addStylesheetIfPresent(dialogPane, CSS_STYLES);
        addStylesheetIfPresent(dialogPane, CSS_COMPONENTS);
    }

    private static void addStylesheetIfPresent(DialogPane dialogPane, String resourcePath) {
        URL url = Modal.class.getResource(resourcePath);
        if (url != null) {
            String externalForm = url.toExternalForm();
            if (!dialogPane.getStylesheets().contains(externalForm)) {
                dialogPane.getStylesheets().add(externalForm);
            }
        }
    }
}
