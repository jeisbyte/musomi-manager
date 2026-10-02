package com.musomi.desktop.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AddUserController {

    private String createdName;
    private String createdUsername;
    private String createdRole;


    @FXML
    private TextField fullNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private void initialize() {

        roleComboBox.getItems().addAll(
                "Administrator",
                "Teacher"
        );
    }

    @FXML
    private void handleCancel() {

        roleComboBox.getScene()
                .getWindow()
                .hide();
    }

    @FXML
    private void handleCreateUser() {

        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        if (fullName.isEmpty()
                || username.isEmpty()
                || password.isEmpty()
                || role == null) {

            showAlert(
                    "Missing Information",
                    "Please fill in all fields."
            );

            return;
        }

        createdName = fullName;
        createdUsername = username;
        createdRole = role;

        roleComboBox.getScene()
                .getWindow()
                .hide();
    }

    private void showAlert(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    public String getCreatedName() {
        return createdName;
    }

    public String getCreatedUsername() {
        return createdUsername;
    }

    public String getCreatedRole() {
        return createdRole;
    }

}