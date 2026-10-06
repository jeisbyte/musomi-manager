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

public class ClassesController {

    @FXML
    private TableView<ClassRow> classesTable;

    @FXML
    private TableColumn<ClassRow, String> nameColumn;

    @FXML
    private TableColumn<ClassRow, String> levelColumn;

    @FXML
    private TableColumn<ClassRow, String> studentsColumn;

    @FXML
    private TableColumn<ClassRow, String> statusColumn;

    @FXML
    private TableColumn<ClassRow, String> actionsColumn;

    @FXML
    private TextField searchField;

    private final ObservableList<ClassRow> allClasses =
            FXCollections.observableArrayList();

    @FXML
    private void initialize() {

        nameColumn.setCellValueFactory(
                data -> data.getValue().nameProperty()
        );

        levelColumn.setCellValueFactory(
                data -> data.getValue().levelProperty()
        );

        studentsColumn.setCellValueFactory(
                data -> data.getValue().studentsProperty()
        );

        statusColumn.setCellValueFactory(
                data -> data.getValue().statusProperty()
        );

        actionsColumn.setCellFactory(column ->
                new TableCell<ClassRow, String>() {

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
                        // Standard table action styling
                        editButton.getStyleClass().add(
                                "table-action-button"
                        );

                        deactivateButton.getStyleClass().addAll(
                                "table-action-button",
                                "table-action-danger"
                        );

                        editButton.setOnAction(event -> {

                            ClassRow classRow =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            handleEditClass(classRow);
                        });

                        deactivateButton.setOnAction(event -> {

                            ClassRow classRow =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            handleDeactivateClass(classRow);
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

        loadSampleClasses();

        classesTable.setItems(allClasses);

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        filterClasses(newValue)
        );
    }

    private void loadSampleClasses() {

        allClasses.addAll(

                new ClassRow(
                        "Senior One",
                        "O-Level",
                        "45",
                        "Active"
                ),

                new ClassRow(
                        "Senior Two",
                        "O-Level",
                        "42",
                        "Active"
                ),

                new ClassRow(
                        "Senior Three",
                        "O-Level",
                        "38",
                        "Active"
                ),

                new ClassRow(
                        "Senior Four",
                        "O-Level",
                        "35",
                        "Inactive"
                )
        );
    }

    private void filterClasses(String searchText) {

        String search =
                searchText.toLowerCase().trim();

        if (search.isEmpty()) {

            classesTable.setItems(allClasses);

            return;
        }

        ObservableList<ClassRow> filteredClasses =
                FXCollections.observableArrayList();

        for (ClassRow classRow : allClasses) {

            if (classRow.getName()
                    .toLowerCase()
                    .contains(search)

                    || classRow.getLevel()
                    .toLowerCase()
                    .contains(search)

                    || classRow.getStatus()
                    .toLowerCase()
                    .contains(search)) {

                filteredClasses.add(classRow);
            }
        }

        classesTable.setItems(filteredClasses);
    }

    @FXML
    private void showAddClassDialog() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/admin/add-class.fxml"
                    )
            );

            Parent root = loader.load();

            AddClassController controller =
                    loader.getController();

            Stage dialog = new Stage();

            dialog.setTitle("Add Class");

            dialog.initModality(
                    Modality.APPLICATION_MODAL
            );

            Scene scene = createStyledScene(root);

            dialog.setScene(scene);

            dialog.setResizable(false);

            dialog.showAndWait();

            if (controller.getCreatedName() != null) {

                ClassRow newClass = new ClassRow(
                        controller.getCreatedName(),
                        controller.getCreatedLevel(),
                        "0",
                        "Active"
                );

                allClasses.add(newClass);

                filterClasses(
                        searchField.getText()
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void handleEditClass(ClassRow classRow) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/admin/edit-class.fxml"
                    )
            );

            Parent root = loader.load();

            EditClassController controller =
                    loader.getController();

            controller.setClassRow(classRow);

            Stage dialog = new Stage();

            dialog.setTitle("Edit Class");

            dialog.initModality(
                    Modality.APPLICATION_MODAL
            );

            Scene scene = createStyledScene(root);

            dialog.setScene(scene);

            dialog.setResizable(false);

            dialog.showAndWait();

            classesTable.refresh();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     * Applies the same CSS used by the main
     * Musomi Manager application.
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

    private void handleDeactivateClass(
            ClassRow classRow
    ) {

        classRow.setStatus("Inactive");

        classesTable.refresh();
    }

    public static class ClassRow {

        private final SimpleStringProperty name;

        private final SimpleStringProperty level;

        private final SimpleStringProperty students;

        private final SimpleStringProperty status;

        public ClassRow(
                String name,
                String level,
                String students,
                String status
        ) {

            this.name =
                    new SimpleStringProperty(name);

            this.level =
                    new SimpleStringProperty(level);

            this.students =
                    new SimpleStringProperty(students);

            this.status =
                    new SimpleStringProperty(status);
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }

        public SimpleStringProperty levelProperty() {
            return level;
        }

        public SimpleStringProperty studentsProperty() {
            return students;
        }

        public SimpleStringProperty statusProperty() {
            return status;
        }

        public String getName() {
            return name.get();
        }

        public String getLevel() {
            return level.get();
        }

        public String getStudents() {
            return students.get();
        }

        public String getStatus() {
            return status.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public void setLevel(String level) {
            this.level.set(level);
        }

        public void setStatus(String status) {
            this.status.set(status);
        }
    }
}