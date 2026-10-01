package com.musomi.desktop.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class AddTopicController {

    @FXML
    private TextField nameField;

    @FXML
    private ComboBox<String> subjectComboBox;

    private String createdName;

    private String createdSubject;

    @FXML
    private void initialize() {

        subjectComboBox.getItems().addAll(
                "Mathematics",
                "English",
                "Physics",
                "History"
        );
    }

    @FXML
    private void handleCancel() {

        subjectComboBox.getScene()
                .getWindow()
                .hide();
    }

    @FXML
    private void handleCreateTopic() {

        String name =
                nameField.getText().trim();

        String subject =
                subjectComboBox.getValue();

        if (name.isEmpty() || subject == null) {

            showAlert(
                    "Missing Information",
                    "Please fill in all fields."
            );

            return;
        }

        createdName = name;

        createdSubject = subject;

        subjectComboBox.getScene()
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

    public String getCreatedSubject() {
        return createdSubject;
    }
}