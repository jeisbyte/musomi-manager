package com.musomi.desktop.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public class AddUserController {

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
        roleComboBox.getScene().getWindow().hide();
    }
}