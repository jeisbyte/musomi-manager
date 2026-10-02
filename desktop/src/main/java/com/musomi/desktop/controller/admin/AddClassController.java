package com.musomi.desktop.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class AddClassController {

    @FXML
    private TextField nameField;

    @FXML
    private ComboBox<String> levelComboBox;

    private String createdName;
    private String createdLevel;

    @FXML
    private void initialize() {

        levelComboBox.getItems().addAll(
                "O-Level",
                "A-Level"
        );
    }

    @FXML
    private void handleCancel() {

        levelComboBox.getScene()
                .getWindow()
                .hide();
    }

    @FXML
    private void handleCreateClass() {

        String name = nameField.getText().trim();

        String level = levelComboBox.getValue();

        if (name.isEmpty() || level == null) {

            showAlert(
                    "Missing Information",
                    "Please fill in all fields."
            );

            return;
        }

        createdName = name;
        createdLevel = level;

        levelComboBox.getScene()
                .getWindow()
                .hide();
    }

    private void showAlert(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    public String getCreatedName() {
        return createdName;
    }

    public String getCreatedLevel() {
        return createdLevel;
    }
}