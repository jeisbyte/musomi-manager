package com.musomi.desktop.controller;

import com.musomi.desktop.controller.teacher.AssessmentStore.Assessment;
import com.musomi.desktop.controller.teacher.MarkEntryController;
import com.musomi.desktop.config.SceneManager;
import com.musomi.desktop.config.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainLayoutController {

    private static final Logger log =
            LoggerFactory.getLogger(MainLayoutController.class);

    @FXML
    private StackPane contentArea;

    @FXML
    private VBox adminNavSection;

    @FXML
    private VBox teacherNavSection;

    @FXML
    private Button dashboardButton;

    @FXML
    private Button markEntryButton;

    @FXML
    private Button myClassesButton;

    @FXML
    private Button assessmentsButton;


    @FXML
    private void initialize() {

        // We are currently developing the Teacher UI.
        // Show only Teacher navigation for now.

        if (adminNavSection != null) {
            adminNavSection.setVisible(false);
            adminNavSection.setManaged(false);
        }

        if (teacherNavSection != null) {
            teacherNavSection.setVisible(true);
            teacherNavSection.setManaged(true);
        }

        // Open Dashboard when the application starts.
        showDashboard();
    }


    // =========================================================
    // DASHBOARD
    // =========================================================

    @FXML
    private void showDashboard() {

        setActiveButton(dashboardButton);

        loadView("/fxml/dashboard.fxml");
    }


    // =========================================================
    // MARK ENTRY
    // =========================================================

    @FXML
    private void showMarkEntry() {
        openMarkEntry(null);
    }

    private void openMarkEntry(Assessment assessment) {
        setActiveButton(markEntryButton);

        loadView("/fxml/teacher/mark-entry.fxml", assessment);
    }


    // =========================================================
    // MY CLASSES
    // =========================================================

    @FXML
    private void showMyClasses() {

        setActiveButton(myClassesButton);

        loadView("/fxml/teacher/my-classes.fxml");
    }


    // =========================================================
    // ASSESSMENTS
    // =========================================================

    @FXML
    private void showAssessments() {

        setActiveButton(assessmentsButton);

        loadView("/fxml/teacher/assessments.fxml");
    }


    // =========================================================
    // ADMIN NAVIGATION
    // =========================================================

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


    // =========================================================
    // ACTIVE NAVIGATION BUTTON
    // =========================================================

    private static final String INACTIVE_STYLE =
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #475569;" +
            "-fx-font-size: 14px;" +
            "-fx-alignment: CENTER_LEFT;" +
            "-fx-padding: 11px 14px;" +
            "-fx-cursor: hand;";

    private static final String ACTIVE_STYLE =
            "-fx-background-color: #eef4ff;" +
            "-fx-background-radius: 8px;" +
            "-fx-text-fill: #2563eb;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-alignment: CENTER_LEFT;" +
            "-fx-padding: 11px 14px;" +
            "-fx-cursor: hand;";

    private void setActiveButton(Button activeButton) {

        // Reset all tracked nav buttons to inactive
        if (dashboardButton   != null) dashboardButton.setStyle(INACTIVE_STYLE);
        if (markEntryButton   != null) markEntryButton.setStyle(INACTIVE_STYLE);
        if (myClassesButton   != null) myClassesButton.setStyle(INACTIVE_STYLE);
        if (assessmentsButton != null) assessmentsButton.setStyle(INACTIVE_STYLE);

        // Apply active style to the selected button
        if (activeButton != null) {
            activeButton.setStyle(ACTIVE_STYLE);
        }
    }


    // =========================================================
    // VIEW LOADING
    // =========================================================

    private void loadView(String fxmlPath) {
        loadView(fxmlPath, null);
    }

    private void loadView(String fxmlPath, Assessment assessment) {
        if (!Session.getInstance().isAuthenticated()) {
            SceneManager.getInstance().switchTo("/fxml/login.fxml");
            return;
        }

        try {

            System.out.println("========================================");
            System.out.println("Loading view: " + fxmlPath);

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(fxmlPath)
                    );

            Node view = loader.load();

            if (loader.getController() instanceof DashboardController dashboardController) {
                dashboardController.setOnOpenMarkEntry(this::openMarkEntry);
            } else if (loader.getController() instanceof MarkEntryController markEntryController
                    && assessment != null) {
                markEntryController.setData(assessment);
            }

            contentArea.getChildren().setAll(view);

            System.out.println(
                    "Successfully loaded: " + fxmlPath
            );

            System.out.println("========================================");

        } catch (Exception e) {

            System.out.println("========================================");
            System.out.println(
                    "FAILED TO LOAD: " + fxmlPath
            );
            System.out.println("========================================");

            e.printStackTrace();

            System.out.println("========================================");
        }
    }
}
