package com.musomi.desktop.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class SettingsController {

    @FXML
    private TextField schoolNameField;

    @FXML
    private TextField academicYearField;

    @FXML
    private ComboBox<String> termComboBox;

    @FXML
    private void initialize() {

        termComboBox.getItems().addAll(
                "Term 1",
                "Term 2",
                "Term 3"
        );

        schoolNameField.setText("Musomi School");
        academicYearField.setText("2026");
        termComboBox.setValue("Term 1");
    }

    @FXML
    private void handleSave() {

        String schoolName =
                schoolNameField.getText().trim();

        String academicYear =
                academicYearField.getText().trim();

        String term =
                termComboBox.getValue();

        if (schoolName.isEmpty()
                || academicYear.isEmpty()
                || term == null) {

            showAlert(
                    "Missing Information",
                    "Please fill in all fields."
            );

            return;
        }

        showAlert(
                "Settings Saved",
                "System settings have been saved successfully."
        );
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
}