package com.musomi.desktop.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import com.musomi.desktop.api.ApiException;
import com.musomi.desktop.config.AppConfig;
import com.musomi.desktop.config.SceneManager;
import com.musomi.desktop.service.AuthService;
import com.musomi.desktop.util.TaskRunner;

/**
 * Controller for the login screen ({@code /fxml/login.fxml}).
 *
 * <p>Handles user authentication via {@link AuthService} on a background thread,
 * stores the resulting session, and navigates to the main application layout on success.
 *
 * <p>Architecture rule: this controller never calls {@link com.musomi.desktop.api.AuthApi}
 * directly — it always goes through {@link AuthService}.
 *
 * <p>Error codes handled per {@code ERROR_CODES.md}:
 * <ul>
 *   <li>{@code INVALID_CREDENTIALS} — wrong username or password (401)</li>
 *   <li>{@code ACCOUNT_LOCKED}      — too many failed attempts (403)</li>
 *   <li>{@code ACCOUNT_INACTIVE}    — account deactivated by admin (403)</li>
 * </ul>
 */
public class LoginController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    private static final String MAIN_LAYOUT_FXML = "/fxml/main-layout.fxml";

    // -------------------------------------------------------------------------
    // FXML field bindings — must match fx:id in login.fxml
    // -------------------------------------------------------------------------

    @FXML private VBox leftPanel;
    @FXML private VBox rightPanel;
    @FXML private Label schoolNameLabel;
    @FXML private Label versionLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private ProgressIndicator spinner;

    // -------------------------------------------------------------------------
    // Collaborators
    // -------------------------------------------------------------------------

    private final AuthService authService = new AuthService();

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Initialises the login screen.
     *
     * <ul>
     *   <li>Populates the version label from {@link AppConfig}.</li>
     *   <li>Binds {@code managed} to {@code visible} so hidden nodes collapse in layout.</li>
     * </ul>
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        versionLabel.setText("Musomi Manager " + AppConfig.getInstance().getAppVersion());

        // Collapse nodes from layout flow when they are not visible
        errorLabel.managedProperty().bind(errorLabel.visibleProperty());
        spinner.managedProperty().bind(spinner.visibleProperty());
    }

    // -------------------------------------------------------------------------
    // Event handlers
    // -------------------------------------------------------------------------

    /**
     * Handles login submission.
     *
     * <p>Triggered by:
     * <ul>
     *   <li>Pressing Enter in the username or password field ({@code onAction})</li>
     *   <li>Clicking the Sign In button ({@code onAction})</li>
     * </ul>
     *
     * <p>The API call is executed off the JavaFX Application Thread via
     * {@link TaskRunner} to prevent UI freezing.
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter your username and password.");
            return;
        }

        log.debug("Login attempt — user: {}", username);

        clearError();
        setLoading(true);

        TaskRunner.run(
            () -> authService.login(username, password),
            response -> {
                log.debug("Login successful — user: {}", username);
                SceneManager.getInstance().switchTo(MAIN_LAYOUT_FXML);
            },
            error -> {
                log.debug("Login failed — user: {} — reason: {}", username, error.getMessage());
                setLoading(false);
                showError(resolveErrorMessage(error));
            }
        );
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Enables or disables the loading state (spinner visible, button disabled).
     *
     * @param loading {@code true} to enter loading state, {@code false} to exit
     */
    private void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        spinner.setVisible(loading);
    }

    /**
     * Hides and clears the error label.
     */
    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setText("");
    }

    /**
     * Shows a user-facing error message below the password field.
     *
     * @param message plain-language error text
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Maps an {@link ApiException} code to a user-facing message per
     * {@code ERROR_CODES.md}. Falls back to the exception message for
     * unknown codes, or a connectivity message for non-API errors.
     *
     * @param error the exception thrown by {@link AuthService#login}
     * @return a plain-language error string suitable for display
     */
    private String resolveErrorMessage(Exception error) {
        if (error instanceof ApiException apiEx) {
            return switch (apiEx.getCode()) {
                // 401 — wrong username or password; never reveal which one failed
                case "INVALID_CREDENTIALS" -> "Invalid username or password.";

                // 403 — server message already contains the lock duration (e.g. "15 minutes")
                case "ACCOUNT_LOCKED"      -> apiEx.getMessage();

                // 403 — account deactivated by an administrator
                case "ACCOUNT_INACTIVE"    -> "Your account has been deactivated. Contact an administrator.";

                // Any other API error — surface the server message directly
                default                    -> apiEx.getMessage();
            };
        }
        // Network or unexpected runtime error
        return "Unable to connect. Please check your network and try again.";
    }
}
