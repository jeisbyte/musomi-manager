package com.musomi.desktop.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Objects;

/**
 * Singleton service that owns the application's primary {@link Stage} and
 * handles all scene-switching for the Musomi Manager desktop client.
 *
 * <p>Usage:
 * <pre>{@code
 * // Simple navigation
 * SceneManager.getInstance().switchTo("/fxml/dashboard.fxml");
 *
 * // Navigation with data passed to the controller
 * SceneManager.getInstance().switchTo("/fxml/detail.fxml", myRecord);
 * }</pre>
 *
 * <p>Every scene loaded by this manager automatically receives the shared
 * stylesheets ({@code /css/styles.css} and {@code /css/components.css}).
 */
public final class SceneManager {

    private static final Logger log = LoggerFactory.getLogger(SceneManager.class);

    private static final String CSS_STYLES     = "/css/styles.css";
    private static final String CSS_COMPONENTS = "/css/components.css";

    /** The method name controllers must expose to receive navigation data. */
    private static final String SET_DATA_METHOD = "setData";

    // -------------------------------------------------------------------------
    // Singleton
    // -------------------------------------------------------------------------

    private static final class Holder {
        private static final SceneManager INSTANCE = new SceneManager();
    }

    private SceneManager() {}

    /**
     * Returns the application-wide singleton instance.
     *
     * @return the {@code SceneManager} singleton
     */
    public static SceneManager getInstance() {
        return Holder.INSTANCE;
    }

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    /** The JavaFX primary stage registered by {@link com.musomi.desktop.DesktopApplication}. */
    private Stage primaryStage;

    // -------------------------------------------------------------------------
    // Stage registration
    // -------------------------------------------------------------------------

    /**
     * Registers the primary {@link Stage}. Must be called once during
     * {@code Application.start()} before any navigation is attempted.
     *
     * @param stage the primary stage; must not be {@code null}
     * @throws IllegalArgumentException if {@code stage} is {@code null}
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = Objects.requireNonNull(stage, "primaryStage must not be null");
        log.debug("Primary stage registered: {}", stage);
    }

    /**
     * Returns the primary stage, or {@code null} if it has not yet been set.
     *
     * @return the primary {@link Stage}, possibly {@code null}
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    // -------------------------------------------------------------------------
    // Navigation — simple
    // -------------------------------------------------------------------------

    /**
     * Loads the FXML at {@code fxmlPath} and replaces the current scene root.
     *
     * @param fxmlPath classpath-relative FXML path (e.g. {@code "/fxml/login.fxml"})
     * @throws NavigationException if the FXML cannot be loaded or the stage is not set
     */
    public void switchTo(String fxmlPath) {
        switchTo(fxmlPath, null);
    }

    // -------------------------------------------------------------------------
    // Navigation — with data
    // -------------------------------------------------------------------------

    /**
     * Loads the FXML at {@code fxmlPath}, replaces the current scene root, and
     * — if {@code data} is non-{@code null} — calls {@code setData(data)} on
     * the controller via reflection if that method exists.
     *
     * @param fxmlPath classpath-relative FXML path (e.g. {@code "/fxml/detail.fxml"})
     * @param data     optional payload forwarded to the controller; may be {@code null}
     * @throws NavigationException if the FXML cannot be loaded or the stage is not set
     */
    public void switchTo(String fxmlPath, Object data) {
        requireStage();

        log.debug("Navigating to: {}", fxmlPath);

        try {
            URL url = Objects.requireNonNull(
                    getClass().getResource(fxmlPath),
                    "FXML resource not found: " + fxmlPath
            );

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            // Pass data to the controller if it exposes setData(Object)
            if (data != null) {
                injectData(loader.getController(), data, fxmlPath);
            }

            // Obtain or create the scene, then update its root
            Scene scene = primaryStage.getScene();
            if (scene == null) {
                scene = new Scene(root);
            } else {
                scene.setRoot(root);
            }

            applyStylesheets(scene);

            primaryStage.setScene(scene);
            log.debug("Navigation complete: {}", fxmlPath);

        } catch (IOException ex) {
            String msg = "Failed to load FXML: " + fxmlPath;
            log.error(msg, ex);
            throw new NavigationException(msg, ex);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Ensures {@link #primaryStage} has been set before any navigation attempt.
     */
    private void requireStage() {
        if (primaryStage == null) {
            throw new IllegalStateException(
                    "Primary stage has not been set. "
                            + "Call SceneManager.getInstance().setPrimaryStage(stage) "
                            + "in Application.start() before navigating."
            );
        }
    }

    /**
     * Applies the shared CSS stylesheets to {@code scene}, adding any that are
     * not already present (avoids duplicates on scene reuse).
     *
     * @param scene the scene to style
     */
    private void applyStylesheets(Scene scene) {
        addStylesheetIfAbsent(scene, CSS_STYLES);
        addStylesheetIfAbsent(scene, CSS_COMPONENTS);
    }

    private void addStylesheetIfAbsent(Scene scene, String resourcePath) {
        URL url = getClass().getResource(resourcePath);
        if (url == null) {
            log.warn("Stylesheet resource not found, skipping: {}", resourcePath);
            return;
        }
        String externalForm = url.toExternalForm();
        if (!scene.getStylesheets().contains(externalForm)) {
            scene.getStylesheets().add(externalForm);
        }
    }

    /**
     * Reflectively calls {@code controller.setData(data)} if the method exists.
     * Mismatched parameter types are logged at WARN and silently skipped.
     *
     * @param controller the controller object returned by {@link FXMLLoader#getController()}
     * @param data       the payload to inject
     * @param fxmlPath   used only for log context
     */
    private void injectData(Object controller, Object data, String fxmlPath) {
        if (controller == null) {
            log.debug("No controller defined for {}, skipping data injection", fxmlPath);
            return;
        }

        try {
            Method setData = controller.getClass().getMethod(SET_DATA_METHOD, Object.class);
            setData.invoke(controller, data);
            log.debug("Data injected into controller {} via setData()", controller.getClass().getSimpleName());
        } catch (NoSuchMethodException ex) {
            // Controller does not declare setData(Object) — this is fine
            log.debug("Controller {} has no setData(Object) method, skipping data injection",
                    controller.getClass().getSimpleName());
        } catch (ReflectiveOperationException ex) {
            log.warn("setData() invocation failed for controller {} (fxml={}): {}",
                    controller.getClass().getSimpleName(), fxmlPath, ex.getMessage(), ex);
        }
    }

    // -------------------------------------------------------------------------
    // NavigationException
    // -------------------------------------------------------------------------

    /**
     * Unchecked exception thrown when a scene transition fails.
     *
     * <p>Wraps the underlying {@link IOException} so callers are not forced to
     * handle checked exceptions on navigation call sites. Replace with a
     * project-level {@code ApiException} once that class is available.
     */
    public static final class NavigationException extends RuntimeException {

        public NavigationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
