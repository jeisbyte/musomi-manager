package com.musomi.desktop.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class AddSubjectController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField codeField;

    private String createdName;

    private String createdCode;

    @FXML
    private void handleCancel() {

        codeField.getScene()
                .getWindow()
                .hide();
    }

    @FXML
    private void handleCreateSubject() {

        String name =
                nameField.getText().trim();

        String code =
                codeField.getText().trim();

        if (name.isEmpty() || code.isEmpty()) {

            showAlert(
                    "Missing Information",
                    "Please fill in all fields."
            );

            return;
        }

        createdName = name;

        createdCode = code;

        codeField.getScene()
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

    public String getCreatedCode() {
        return createdCode;
    }
}