package com.musomi.desktop.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class EditTopicController {

    @FXML
    private TextField nameField;

    @FXML
    private ComboBox<String> subjectComboBox;

    @FXML
    private ComboBox<String> statusComboBox;

    private TopicsController.TopicRow topicRow;

    @FXML
    private void initialize() {

        subjectComboBox.getItems().addAll(
                "Mathematics",
                "English",
                "Physics",
                "History"
        );

        statusComboBox.getItems().addAll(
                "Active",
                "Inactive"
        );
    }

    public void setTopicRow(
            TopicsController.TopicRow topicRow
    ) {

        this.topicRow = topicRow;

        nameField.setText(topicRow.getName());

        subjectComboBox.setValue(
                topicRow.getSubject()
        );

        statusComboBox.setValue(
                topicRow.getStatus()
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

        if (topicRow == null) {
            return;
        }

        String name =
                nameField.getText().trim();

        String subject =
                subjectComboBox.getValue();

        String status =
                statusComboBox.getValue();

        if (name.isEmpty()
                || subject == null
                || status == null) {

            return;
        }

        topicRow.setName(name);

        topicRow.setSubject(subject);

        topicRow.setStatus(status);

        statusComboBox.getScene()
                .getWindow()
                .hide();
    }
}