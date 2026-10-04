package com.musomi.desktop.controller.teacher;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import com.musomi.desktop.api.ApiException;
import com.musomi.desktop.config.SceneManager;
import com.musomi.desktop.config.Session;
import com.musomi.desktop.model.dto.TeacherClassResponse;
import com.musomi.desktop.service.TeacherClassService;
import com.musomi.desktop.util.TaskRunner;
import com.musomi.desktop.util.Toast;

/**
 * Controller for the My Classes screen ({@code /fxml/teacher/my-classes.fxml}).
 *
 * <p>Loads the authenticated teacher's assigned classes via {@link TeacherClassService}
 * on a background thread and populates a {@link TableView} on success.
 *
 * <p>Architecture rule: this controller never calls {@link com.musomi.desktop.api.TeacherClassApi}
 * directly — it always goes through {@link TeacherClassService}.
 *
 * <p>Error codes handled per {@code ERROR_CODES.md}:
 * <ul>
 *   <li>{@code SESSION_EXPIRED} / {@code HTTP_401} — session expired; redirect to login</li>
 *   <li>{@code PERMISSION_DENIED} / {@code ADMIN_ONLY} — wrong role; redirect to login</li>
 *   <li>{@code NETWORK_ERROR} — connectivity failure; show inline error + toast</li>
 *   <li>Default — surface server message via Toast and inline error label</li>
 * </ul>
 */
public class MyClassesController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(MyClassesController.class);

    private static final String LOGIN_FXML = "/fxml/login.fxml";

    // -------------------------------------------------------------------------
    // FXML bindings — must match fx:id in my-classes.fxml
    // -------------------------------------------------------------------------

    @FXML private TableView<TeacherClassResponse> classesTable;
    @FXML private TableColumn<TeacherClassResponse, String>  classColumn;
    @FXML private TableColumn<TeacherClassResponse, String>  streamColumn;
    @FXML private TableColumn<TeacherClassResponse, String>  subjectColumn;
    @FXML private TableColumn<TeacherClassResponse, Number>  studentsColumn;
    @FXML private TableColumn<TeacherClassResponse, Number>  assessmentsColumn;
    @FXML private TableColumn<TeacherClassResponse, Number>  draftsColumn;

    @FXML private ProgressIndicator loadingSpinner;
    @FXML private VBox              loadingPane;
    @FXML private VBox              emptyPane;
    @FXML private VBox              errorPane;
    @FXML private Label             errorMessageLabel;
    @FXML private Button            refreshButton;

    // -------------------------------------------------------------------------
    // Collaborators
    // -------------------------------------------------------------------------

    private final TeacherClassService classService = new TeacherClassService();
    private final Session             session      = Session.getInstance();

    private final ObservableList<TeacherClassResponse> classes =
            FXCollections.observableArrayList();

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Role guard: this screen must not be reachable by non-teachers
        if (!isTeacher()) {
            log.warn("Non-teacher attempted to access My Classes — role={}", session.getRole());
            redirectToLogin();
            return;
        }

        setupTableColumns();
        classesTable.setItems(classes);

        // Collapse hidden panes from layout flow
        loadingPane.managedProperty().bind(loadingPane.visibleProperty());
        emptyPane.managedProperty().bind(emptyPane.visibleProperty());
        errorPane.managedProperty().bind(errorPane.visibleProperty());

        loadClasses();
    }

    // -------------------------------------------------------------------------
    // Event handlers
    // -------------------------------------------------------------------------

    /**
     * Handles the Refresh button click. Re-fetches classes from the backend.
     */
    @FXML
    private void handleRefresh() {
        loadClasses();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Configures cell-value factories for each table column.
     * Uses property wrappers so JavaFX observability is respected.
     */
    private void setupTableColumns() {
        classColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getClassName())
        );
        streamColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getStreamName())
        );
        subjectColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getSubjectName())
        );
        studentsColumn.setCellValueFactory(
                data -> new SimpleIntegerProperty(data.getValue().getStudentCount())
        );
        assessmentsColumn.setCellValueFactory(
                data -> new SimpleIntegerProperty(data.getValue().getAssessmentCount())
        );
        draftsColumn.setCellValueFactory(
                data -> new SimpleIntegerProperty(data.getValue().getDraftCount())
        );
    }

    /**
     * Triggers the background load of teacher classes.
     * Moves through loading → (success | error) states.
     */
    private void loadClasses() {
        showLoading();

        TaskRunner.run(
            classService::getMyClasses,
            this::onLoadSuccess,
            this::onLoadError
        );
    }

    /**
     * Called on the JavaFX Application Thread when the backend request succeeds.
     *
     * @param result the list of class assignments returned by the server
     */
    private void onLoadSuccess(List<TeacherClassResponse> result) {
        log.debug("Loaded {} teacher class(es)", result == null ? 0 : result.size());

        classes.clear();

        if (result == null || result.isEmpty()) {
            showEmpty();
            return;
        }

        classes.addAll(result);
        showTable();
    }

    /**
     * Called on the JavaFX Application Thread when the backend request fails.
     * Handles known error codes per ERROR_CODES.md and falls back to a generic message.
     *
     * @param error the exception thrown during the background task
     */
    private void onLoadError(Exception error) {
        log.warn("Failed to load teacher classes: {}", error.getMessage());

        if (error instanceof ApiException apiEx) {
            String code = apiEx.getCode();
            switch (code) {
                case "SESSION_EXPIRED", "HTTP_401" -> {
                    // Session has expired — clear and redirect to login
                    log.warn("Session expired. Redirecting to login.");
                    session.clear();
                    redirectToLogin();
                    return;
                }
                case "PERMISSION_DENIED", "ADMIN_ONLY", "HTTP_403" -> {
                    // Access denied — user is not allowed to view this screen
                    log.warn("Access denied for My Classes. Redirecting to login.");
                    redirectToLogin();
                    return;
                }
                case "NETWORK_ERROR" -> {
                    String msg = "Could not connect to the server. Please check your network and try again.";
                    showError(msg);
                    Toast.error(msg);
                }
                default -> {
                    String msg = apiEx.getMessage() != null
                            ? apiEx.getMessage()
                            : "An unexpected error occurred. Please try again.";
                    showError(msg);
                    Toast.error(msg);
                }
            }
        } else {
            String msg = "Could not connect to the server. Please check your network and try again.";
            showError(msg);
            Toast.error(msg);
        }
    }

    // -------------------------------------------------------------------------
    // UI State Management
    // -------------------------------------------------------------------------

    private void showLoading() {
        loadingPane.setVisible(true);
        emptyPane.setVisible(false);
        errorPane.setVisible(false);
        classesTable.setVisible(false);
    }

    private void showTable() {
        loadingPane.setVisible(false);
        emptyPane.setVisible(false);
        errorPane.setVisible(false);
        classesTable.setVisible(true);
    }

    private void showEmpty() {
        loadingPane.setVisible(false);
        emptyPane.setVisible(true);
        errorPane.setVisible(false);
        classesTable.setVisible(false);
    }

    private void showError(String message) {
        errorMessageLabel.setText(message);
        loadingPane.setVisible(false);
        emptyPane.setVisible(false);
        errorPane.setVisible(true);
        classesTable.setVisible(false);
    }

    // -------------------------------------------------------------------------
    // Role Safety
    // -------------------------------------------------------------------------

    /**
     * Returns true if the authenticated user is a TEACHER.
     * Uses the actual role field from Session, as stored by AuthService on login.
     */
    private boolean isTeacher() {
        String role = session.getRole();
        return "TEACHER".equalsIgnoreCase(role);
    }

    /**
     * Redirects to the login screen. Used when the session is invalid or
     * the user does not have the required role.
     */
    private void redirectToLogin() {
        SceneManager.getInstance().switchTo(LOGIN_FXML);
    }
}
