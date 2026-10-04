package com.musomi.desktop.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import com.musomi.desktop.config.SceneManager;
import com.musomi.desktop.config.Session;

/**
 * Controller for the main application shell ({@code /fxml/main-layout.fxml}).
 *
 * <p>Manages the left-sidebar navigation and central content area.
 * Navigation items are shown or hidden based on the authenticated user's role,
 * as stored in {@link Session#getRole()}.
 *
 * <p>Role visibility rules:
 * <ul>
 *   <li>ADMIN — admin navigation section (Users, Classes, Subjects, Topics, Students, Settings, Audit Log)</li>
 *   <li>TEACHER — teacher navigation section (My Classes)</li>
 * </ul>
 *
 * <p>Backend authorization remains the source of truth. Hiding nav items is a
 * UX convenience; the backend will reject unauthorized requests regardless.
 */
public class MainLayoutController {

    private static final Logger log = LoggerFactory.getLogger(MainLayoutController.class);

    private static final String ROLE_ADMIN   = "ADMIN";
    private static final String ROLE_TEACHER = "TEACHER";

    // -------------------------------------------------------------------------
    // FXML bindings — must match fx:id in main-layout.fxml
    // -------------------------------------------------------------------------

    @FXML private StackPane contentArea;

    /** Navigation section shown only to ADMIN users. */
    @FXML private VBox adminNavSection;

    /** Navigation section shown only to TEACHER users. */
    @FXML private VBox teacherNavSection;

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @FXML
    private void initialize() {
        applyRoleNavigation();
        navigateToDefaultScreen();
    }

    // -------------------------------------------------------------------------
    // Event handlers — shared
    // -------------------------------------------------------------------------

    @FXML
    private void showDashboard() {
        loadView("/fxml/dashboard.fxml");
    }

    // -------------------------------------------------------------------------
    // Event handlers — admin
    // -------------------------------------------------------------------------

    @FXML
    private void showUsers() {
        loadView("/fxml/admin/users.fxml");
    }

    @FXML
    private void showClasses() {
        loadView("/fxml/admin/classes.fxml");
    }

    @FXML
    private void showSubjects() {
        loadView("/fxml/admin/subjects.fxml");
    }

    @FXML
    private void showTopics() {
        loadView("/fxml/admin/topics.fxml");
    }

    @FXML
    private void showStudents() {
        loadView("/fxml/admin/students.fxml");
    }

    @FXML
    private void showSettings() {
        loadView("/fxml/admin/settings.fxml");
    }

    @FXML
    private void showAuditLog() {
        loadView("/fxml/admin/audit-log.fxml");
    }

    // -------------------------------------------------------------------------
    // Event handlers — teacher
    // -------------------------------------------------------------------------

    /**
     * Navigates to the My Classes screen for the authenticated teacher.
     *
     * <p>The controller performs a role check on initialize() and will redirect
     * to login if the user is not a TEACHER, providing a second layer of defence.
     */
    @FXML
    private void showMyClasses() {
        loadView("/fxml/teacher/my-classes.fxml");
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Shows or hides nav sections based on the authenticated user's role.
     * Called once during {@link #initialize()}.
     */
    private void applyRoleNavigation() {
        String role = Session.getInstance().getRole();
        log.debug("Applying navigation for role: {}", role);

        boolean isAdmin   = ROLE_ADMIN.equalsIgnoreCase(role);
        boolean isTeacher = ROLE_TEACHER.equalsIgnoreCase(role);

        setNavSectionVisible(adminNavSection,   isAdmin);
        setNavSectionVisible(teacherNavSection, isTeacher);
    }

    /**
     * Sets the visibility and managed state of a navigation section VBox.
     * Setting managed=false removes the section from layout flow when invisible.
     */
    private void setNavSectionVisible(VBox section, boolean visible) {
        if (section != null) {
            section.setVisible(visible);
            section.setManaged(visible);
        }
    }

    /**
     * Loads the appropriate default screen for the current user's role.
     */
    private void navigateToDefaultScreen() {
        String role = Session.getInstance().getRole();
        if (ROLE_TEACHER.equalsIgnoreCase(role)) {
            loadView("/fxml/teacher/my-classes.fxml");
        } else {
            // ADMIN or any other role — show the admin dashboard
            loadView("/fxml/dashboard.fxml");
        }
    }

    /**
     * Loads an FXML view and places it in the central content area.
     *
     * @param fxmlPath classpath-relative FXML path
     */
    private void loadView(String fxmlPath) {
        try {
            Node view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            log.error("Failed to load view: {}", fxmlPath, e);
        }
    }
}