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

    private void openDialog(
            String fxmlPath,
            String title
    ) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(fxmlPath)
                    );

            Parent root = loader.load();

            Stage dialog = new Stage();

            dialog.setTitle(title);

            dialog.initModality(
                    Modality.APPLICATION_MODAL
            );

            Scene scene = createStyledScene(root);

            dialog.setScene(scene);

            dialog.setResizable(false);

            dialog.showAndWait();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     * Applies the same CSS used by the
     * main Musomi Manager application.
     */
    private Scene createStyledScene(Parent root) {

        Scene scene = new Scene(root);

        scene.getStylesheets().add(
                getClass()
                        .getResource("/css/styles.css")
                        .toExternalForm()
        );

        scene.getStylesheets().add(
                getClass()
                        .getResource("/css/components.css")
                        .toExternalForm()
        );

        return scene;
    }
}