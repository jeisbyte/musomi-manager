package com.musomi.desktop.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import com.musomi.desktop.config.SceneManager;
import com.musomi.desktop.service.AuthService;

public class MainLayoutController {

    @FXML
    private StackPane contentArea;

    @FXML
    private void initialize() {
        showDashboard();
    }

    @FXML
    private void showDashboard() {
        loadView("/fxml/dashboard.fxml");
    }

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

    private void loadView(String fxmlPath) {
        try {
            Node view = FXMLLoader.load(
                    getClass().getResource(fxmlPath)
            );

            contentArea.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogout() {
        try {
            authService.logout();
        } finally {
            SceneManager.getInstance().switchTo("/fxml/login.fxml");
        }
    }

}