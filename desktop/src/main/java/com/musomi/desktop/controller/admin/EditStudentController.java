package com.musomi.desktop.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class EditStudentController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField admissionNumberField;

    @FXML
    private ComboBox<String> classComboBox;

    @FXML
    private ComboBox<String> statusComboBox;

    private StudentsController.StudentRow studentRow;

    @FXML
    private void initialize() {

        classComboBox.getItems().addAll(
                "Senior One",
                "Senior Two",
                "Senior Three",
                "Senior Four"
        );

        statusComboBox.getItems().addAll(
                "Active",
                "Inactive"
        );
    }

    public void setStudentRow(
            StudentsController.StudentRow studentRow
    ) {

        this.studentRow = studentRow;

        nameField.setText(
                studentRow.getName()
        );

        admissionNumberField.setText(
                studentRow.getAdmissionNumber()
        );

        classComboBox.setValue(
                studentRow.getClassName()
        );

        statusComboBox.setValue(
                studentRow.getStatus()
        );
    }

    @FXML
    private void handleCancel() {

        statusComboBox.getScene()
                .getWindow()
                .hide();
    }

    @FXML
    private void handleSave() {

        if (studentRow == null) {
            return;
        }

        String name =
                nameField.getText().trim();

        String admissionNumber =
                admissionNumberField
                        .getText()
                        .trim();

        String className =
                classComboBox.getValue();

        String status =
                statusComboBox.getValue();

        if (name.isEmpty()
                || admissionNumber.isEmpty()
                || className == null
                || status == null) {

            return;
        }

        studentRow.setName(name);

        studentRow.setAdmissionNumber(
                admissionNumber
        );

        studentRow.setClassName(
                className
        );

        studentRow.setStatus(status);

        statusComboBox.getScene()
                .getWindow()
                .hide();
    }
}