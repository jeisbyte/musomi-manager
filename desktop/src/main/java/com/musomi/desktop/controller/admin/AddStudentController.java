package com.musomi.desktop.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class AddStudentController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField admissionNumberField;

    @FXML
    private ComboBox<String> classComboBox;

    private String createdName;

    private String createdAdmissionNumber;

    private String createdClass;

    @FXML
    private void initialize() {

        classComboBox.getItems().addAll(
                "Senior One",
                "Senior Two",
                "Senior Three",
                "Senior Four"
        );
    }

    @FXML
    private void handleCancel() {

        classComboBox.getScene()
                .getWindow()
                .hide();
    }

    @FXML
    private void handleCreateStudent() {

        String name =
                nameField.getText().trim();

        String admissionNumber =
                admissionNumberField
                        .getText()
                        .trim();

        String className =
                classComboBox.getValue();

        if (name.isEmpty()
                || admissionNumber.isEmpty()
                || className == null) {

            showAlert(
                    "Missing Information",
                    "Please fill in all fields."
            );

            return;
        }

        createdName = name;

        createdAdmissionNumber =
                admissionNumber;

        createdClass = className;

        classComboBox.getScene()
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

    public String getCreatedAdmissionNumber() {
        return createdAdmissionNumber;
    }

    public String getCreatedClass() {
        return createdClass;
    }
}