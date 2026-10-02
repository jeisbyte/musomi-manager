package com.musomi.desktop.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class EditClassController {

    @FXML
    private TextField nameField;

    @FXML
    private ComboBox<String> levelComboBox;

    @FXML
    private ComboBox<String> statusComboBox;

    private ClassesController.ClassRow classRow;

    @FXML
    private void initialize() {

        levelComboBox.getItems().addAll(
                "O-Level",
                "A-Level"
        );

        statusComboBox.getItems().addAll(
                "Active",
                "Inactive"
        );
    }

    public void setClassRow(
            ClassesController.ClassRow classRow
    ) {

        this.classRow = classRow;

        nameField.setText(
                classRow.getName()
        );

        levelComboBox.setValue(
                classRow.getLevel()
        );

        statusComboBox.setValue(
                classRow.getStatus()
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

        if (classRow == null) {
            return;
        }

        String name =
                nameField.getText().trim();

        String level =
                levelComboBox.getValue();

        String status =
                statusComboBox.getValue();

        if (name.isEmpty()
                || level == null
                || status == null) {
            return;
        }

        classRow.setName(name);

        classRow.setLevel(level);

        classRow.setStatus(status);

        statusComboBox.getScene()
                .getWindow()
                .hide();
    }
}