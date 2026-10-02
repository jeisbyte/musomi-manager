package com.musomi.desktop.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DashboardController {

    @FXML
    private void handleAddStudent() {
        openDialog(
                "/fxml/admin/add-student.fxml",
                "Add Student"
        );
    }

    @FXML
    private void handleAddClass() {
        openDialog(
                "/fxml/admin/add-class.fxml",
                "Add Class"
        );
    }

    @FXML
    private void handleAddSubject() {
        openDialog(
                "/fxml/admin/add-subject.fxml",
                "Add Subject"
        );
    }

    private void openDialog(String fxmlPath, String title) {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource(fxmlPath));

            Parent root = loader.load();

            Stage dialog = new Stage();

            dialog.setTitle(title);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);

            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}