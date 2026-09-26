package com.musomi.desktop;

import com.musomi.desktop.config.AppConfig;
import com.musomi.desktop.config.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for the Musomi Manager desktop application.
 *
 * <p>Bootstraps the JavaFX runtime, loads the login screen, applies the
 * shared CSS stylesheets, and hands the primary stage to
 * {@link SceneManager} for all subsequent navigation.
 */
public class DesktopApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(DesktopApplication.class);

    private static final double MIN_WIDTH = 1280;
    private static final double MIN_HEIGHT = 720;

    private static final String LOGIN_FXML = "/fxml/login.fxml";

    @Override
    public void start(Stage primaryStage) {
        log.info("Starting Musomi Manager v{}", AppConfig.getInstance().getAppVersion());

        primaryStage.setTitle("Musomi Manager");
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);

        SceneManager.getInstance().setPrimaryStage(primaryStage);
        SceneManager.getInstance().switchTo(LOGIN_FXML);

        primaryStage.show();
        log.info("Application started");
    }

    public static void main(String[] args) {
        launch(args);
    }
}