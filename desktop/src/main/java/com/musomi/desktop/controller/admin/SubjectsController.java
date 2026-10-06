package com.musomi.desktop.controller.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SubjectsController {

    @FXML
    private TableView<SubjectRow> subjectsTable;

    @FXML
    private TableColumn<SubjectRow, String> nameColumn;

    @FXML
    private TableColumn<SubjectRow, String> codeColumn;

    @FXML
    private TableColumn<SubjectRow, String> statusColumn;

    @FXML
    private TableColumn<SubjectRow, String> actionsColumn;

    @FXML
    private TextField searchField;

    private final ObservableList<SubjectRow> allSubjects =
            FXCollections.observableArrayList();

    @FXML
    private void initialize() {

        nameColumn.setCellValueFactory(
                data -> data.getValue().nameProperty()
        );

        codeColumn.setCellValueFactory(
                data -> data.getValue().codeProperty()
        );

        statusColumn.setCellValueFactory(
                data -> data.getValue().statusProperty()
        );

        actionsColumn.setCellFactory(column ->
                new TableCell<SubjectRow, String>() {

                    private final Button editButton =
                            new Button("Edit");

                    private final Button deactivateButton =
                            new Button("Deactivate");

                    private final HBox container =
                            new HBox(
                                    8,
                                    editButton,
                                    deactivateButton
                            );

                    {
                        // Table action button styling
                        editButton.getStyleClass().add(
                                "table-action-button"
                        );

                        deactivateButton.getStyleClass().addAll(
                                "table-action-button",
                                "table-action-danger"
                        );

                        editButton.setOnAction(event -> {

                            SubjectRow subject =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            handleEditSubject(subject);
                        });

                        deactivateButton.setOnAction(event -> {

                            SubjectRow subject =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            handleDeactivateSubject(subject);
                        });
                    }

                    @Override
                    protected void updateItem(
                            String item,
                            boolean empty
                    ) {

                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(container);
                        }
                    }
                }
        );

        loadSampleSubjects();

        subjectsTable.setItems(allSubjects);

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        filterSubjects(newValue)
        );
    }

    private void loadSampleSubjects() {

        allSubjects.addAll(

                new SubjectRow(
                        "Mathematics",
                        "MATH",
                        "Active"
                ),

                new SubjectRow(
                        "English",
                        "ENG",
                        "Active"
                ),

                new SubjectRow(
                        "Physics",
                        "PHY",
                        "Active"
                ),

                new SubjectRow(
                        "History",
                        "HIST",
                        "Inactive"
                )
        );
    }

    private void filterSubjects(String searchText) {

        String search =
                searchText.toLowerCase().trim();

        if (search.isEmpty()) {

            subjectsTable.setItems(allSubjects);

            return;
        }

        ObservableList<SubjectRow> filteredSubjects =
                FXCollections.observableArrayList();

        for (SubjectRow subject : allSubjects) {

            if (subject.getName()
                    .toLowerCase()
                    .contains(search)

                    || subject.getCode()
                    .toLowerCase()
                    .contains(search)

                    || subject.getStatus()
                    .toLowerCase()
                    .contains(search)) {

                filteredSubjects.add(subject);
            }
        }

        subjectsTable.setItems(filteredSubjects);
    }

    @FXML
    private void showAddSubjectDialog() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/admin/add-subject.fxml"
                    )
            );

            Parent root = loader.load();

            AddSubjectController controller =
                    loader.getController();

            Stage dialog = new Stage();

            dialog.setTitle("Add Subject");

            dialog.initModality(
                    Modality.APPLICATION_MODAL
            );

            dialog.setScene(
                    createStyledScene(root)
            );

            dialog.setResizable(false);

            dialog.showAndWait();

            if (controller.getCreatedName() != null) {

                SubjectRow newSubject =
                        new SubjectRow(
                                controller.getCreatedName(),
                                controller.getCreatedCode(),
                                "Active"
                        );

                allSubjects.add(newSubject);

                filterSubjects(
                        searchField.getText()
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void handleEditSubject(
            SubjectRow subject
    ) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/admin/edit-subject.fxml"
                    )
            );

            Parent root = loader.load();

            EditSubjectController controller =
                    loader.getController();

            controller.setSubjectRow(subject);

            Stage dialog = new Stage();

            dialog.setTitle("Edit Subject");

            dialog.initModality(
                    Modality.APPLICATION_MODAL
            );

            dialog.setScene(
                    createStyledScene(root)
            );

            dialog.setResizable(false);

            dialog.showAndWait();

            subjectsTable.refresh();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

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

    private void handleDeactivateSubject(
            SubjectRow subject
    ) {

        subject.setStatus("Inactive");

        subjectsTable.refresh();
    }

    public static class SubjectRow {

        private final SimpleStringProperty name;

        private final SimpleStringProperty code;

        private final SimpleStringProperty status;

        public SubjectRow(
                String name,
                String code,
                String status
        ) {

            this.name =
                    new SimpleStringProperty(name);

            this.code =
                    new SimpleStringProperty(code);

            this.status =
                    new SimpleStringProperty(status);
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }

        public SimpleStringProperty codeProperty() {
            return code;
        }

        public SimpleStringProperty statusProperty() {
            return status;
        }

        public String getName() {
            return name.get();
        }

        public String getCode() {
            return code.get();
        }

        public String getStatus() {
            return status.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public void setCode(String code) {
            this.code.set(code);
        }

        public void setStatus(String status) {
            this.status.set(status);
        }
    }
}