package com.musomi.desktop.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class EditSubjectController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField codeField;

    @FXML
    private ComboBox<String> statusComboBox;

    private SubjectsController.SubjectRow subjectRow;

    @FXML
    private void initialize() {
        statusComboBox.getItems().addAll(
                "Active",
                "Inactive"
        );
    }

    public void setSubjectRow(
            SubjectsController.SubjectRow subjectRow
    ) {
        this.subjectRow = subjectRow;

        nameField.setText(subjectRow.getName());
        codeField.setText(subjectRow.getCode());
        statusComboBox.setValue(subjectRow.getStatus());
    }

    @FXML
    private void handleCancel() {

        statusComboBox.getScene()
                .getWindow()
                .hide();
    }

    @FXML
    private void handleSave() {

        if (subjectRow == null) {
            return;
        }

        String name = nameField.getText().trim();
        String code = codeField.getText().trim();
        String status = statusComboBox.getValue();

        if (name.isEmpty()
                || code.isEmpty()
                || status == null) {

            return;
        }

        subjectRow.setName(name);
        subjectRow.setCode(code);
        subjectRow.setStatus(status);

        statusComboBox.getScene()
                .getWindow()
                .hide();
    }
}