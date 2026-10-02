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

public class StudentsController {

    @FXML
    private TableView<StudentRow> studentsTable;

    @FXML
    private TableColumn<StudentRow, String> nameColumn;

    @FXML
    private TableColumn<StudentRow, String> admissionNumberColumn;

    @FXML
    private TableColumn<StudentRow, String> classColumn;

    @FXML
    private TableColumn<StudentRow, String> statusColumn;

    @FXML
    private TableColumn<StudentRow, String> actionsColumn;

    @FXML
    private TextField searchField;

    private final ObservableList<StudentRow> allStudents =
            FXCollections.observableArrayList();

    @FXML
    private void initialize() {

        nameColumn.setCellValueFactory(
                data -> data.getValue().nameProperty()
        );

        admissionNumberColumn.setCellValueFactory(
                data -> data.getValue().admissionNumberProperty()
        );

        classColumn.setCellValueFactory(
                data -> data.getValue().classNameProperty()
        );

        statusColumn.setCellValueFactory(
                data -> data.getValue().statusProperty()
        );

        actionsColumn.setCellFactory(column ->
                new TableCell<StudentRow, String>() {

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
                        editButton.setOnAction(event -> {

                            StudentRow student =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            handleEditStudent(student);
                        });

                        deactivateButton.setOnAction(event -> {

                            StudentRow student =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            handleDeactivateStudent(student);
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

        loadSampleStudents();

        studentsTable.setItems(allStudents);

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        filterStudents(newValue)
        );
    }

    private void loadSampleStudents() {

        allStudents.addAll(

                new StudentRow(
                        "John Doe",
                        "STU001",
                        "Senior One",
                        "Active"
                ),

                new StudentRow(
                        "Jane Smith",
                        "STU002",
                        "Senior Two",
                        "Active"
                ),

                new StudentRow(
                        "Michael Brown",
                        "STU003",
                        "Senior Three",
                        "Active"
                ),

                new StudentRow(
                        "Sarah Wilson",
                        "STU004",
                        "Senior Four",
                        "Inactive"
                )
        );
    }

    private void filterStudents(String searchText) {

        String search =
                searchText.toLowerCase().trim();

        if (search.isEmpty()) {

            studentsTable.setItems(allStudents);

            return;
        }

        ObservableList<StudentRow> filteredStudents =
                FXCollections.observableArrayList();

        for (StudentRow student : allStudents) {

            if (student.getName()
                    .toLowerCase()
                    .contains(search)

                    || student.getAdmissionNumber()
                    .toLowerCase()
                    .contains(search)

                    || student.getClassName()
                    .toLowerCase()
                    .contains(search)

                    || student.getStatus()
                    .toLowerCase()
                    .contains(search)) {

                filteredStudents.add(student);
            }
        }

        studentsTable.setItems(filteredStudents);
    }

    @FXML
    private void showAddStudentDialog() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/admin/add-student.fxml"
                    )
            );

            Parent root = loader.load();

            AddStudentController controller =
                    loader.getController();

            Stage dialog = new Stage();

            dialog.setTitle("Add Student");

            dialog.initModality(
                    Modality.APPLICATION_MODAL
            );

            dialog.setScene(
                    new Scene(root)
            );

            dialog.setResizable(false);

            dialog.showAndWait();

            if (controller.getCreatedName() != null) {

                StudentRow newStudent =
                        new StudentRow(
                                controller.getCreatedName(),
                                controller.getCreatedAdmissionNumber(),
                                controller.getCreatedClass(),
                                "Active"
                        );

                allStudents.add(newStudent);

                filterStudents(
                        searchField.getText()
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void handleEditStudent(
            StudentRow student
    ) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/admin/edit-student.fxml"
                    )
            );

            Parent root = loader.load();

            EditStudentController controller =
                    loader.getController();

            controller.setStudentRow(student);

            Stage dialog = new Stage();

            dialog.setTitle("Edit Student");

            dialog.initModality(
                    Modality.APPLICATION_MODAL
            );

            dialog.setScene(
                    new Scene(root)
            );

            dialog.setResizable(false);

            dialog.showAndWait();

            studentsTable.refresh();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void handleDeactivateStudent(
            StudentRow student
    ) {

        student.setStatus("Inactive");

        studentsTable.refresh();
    }

    public static class StudentRow {

        private final SimpleStringProperty name;

        private final SimpleStringProperty admissionNumber;

        private final SimpleStringProperty className;

        private final SimpleStringProperty status;

        public StudentRow(
                String name,
                String admissionNumber,
                String className,
                String status
        ) {

            this.name =
                    new SimpleStringProperty(name);

            this.admissionNumber =
                    new SimpleStringProperty(
                            admissionNumber
                    );

            this.className =
                    new SimpleStringProperty(
                            className
                    );

            this.status =
                    new SimpleStringProperty(status);
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }

        public SimpleStringProperty admissionNumberProperty() {
            return admissionNumber;
        }

        public SimpleStringProperty classNameProperty() {
            return className;
        }

        public SimpleStringProperty statusProperty() {
            return status;
        }

        public String getName() {
            return name.get();
        }

        public String getAdmissionNumber() {
            return admissionNumber.get();
        }

        public String getClassName() {
            return className.get();
        }

        public String getStatus() {
            return status.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public void setAdmissionNumber(
                String admissionNumber
        ) {
            this.admissionNumber.set(
                    admissionNumber
            );
        }

        public void setClassName(
                String className
        ) {
            this.className.set(className);
        }

        public void setStatus(String status) {
            this.status.set(status);
        }
    }
}