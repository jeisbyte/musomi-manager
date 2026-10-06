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

public class UsersController {

    @FXML
    private TableView<UserRow> usersTable;

    @FXML
    private TableColumn<UserRow, String> nameColumn;

    @FXML
    private TableColumn<UserRow, String> usernameColumn;

    @FXML
    private TableColumn<UserRow, String> roleColumn;

    @FXML
    private TableColumn<UserRow, String> statusColumn;

    @FXML
    private TableColumn<UserRow, String> actionsColumn;

    @FXML
    private TextField searchField;

    private final ObservableList<UserRow> allUsers =
            FXCollections.observableArrayList();

    @FXML
    private void initialize() {

        nameColumn.setCellValueFactory(
                data -> data.getValue().nameProperty()
        );

        usernameColumn.setCellValueFactory(
                data -> data.getValue().usernameProperty()
        );

        roleColumn.setCellValueFactory(
                data -> data.getValue().roleProperty()
        );

        statusColumn.setCellValueFactory(
                data -> data.getValue().statusProperty()
        );

        actionsColumn.setCellFactory(column -> new TableCell<>() {

            private final Button editButton =
                    new Button("Edit");

            private final Button deactivateButton =
                    new Button("Deactivate");

            private final HBox container =
                    new HBox(8, editButton, deactivateButton);

            {
                // Standard table action styling
                editButton.getStyleClass().add("table-action-button");

                deactivateButton.getStyleClass().addAll(
                        "table-action-button",
                        "table-action-danger"
                );

                editButton.setOnAction(event -> {

                    UserRow user = getTableView()
                            .getItems()
                            .get(getIndex());

                    handleEditUser(user);
                });

                deactivateButton.setOnAction(event -> {

                    UserRow user = getTableView()
                            .getItems()
                            .get(getIndex());

                    handleDeactivateUser(user);
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
        });

        loadSampleUsers();

        usersTable.setItems(allUsers);

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        filterUsers(newValue)
        );
    }

    private void loadSampleUsers() {

        allUsers.addAll(

                new UserRow(
                        "John Doe",
                        "john",
                        "Teacher",
                        "Active"
                ),

                new UserRow(
                        "Mary Admin",
                        "mary",
                        "Administrator",
                        "Active"
                ),

                new UserRow(
                        "Peter Smith",
                        "peter",
                        "Teacher",
                        "Inactive"
                )
        );
    }

    private void filterUsers(String searchText) {

        String search = searchText.toLowerCase().trim();

        if (search.isEmpty()) {
            usersTable.setItems(allUsers);
            return;
        }

        ObservableList<UserRow> filteredUsers =
                FXCollections.observableArrayList();

        for (UserRow user : allUsers) {

            if (user.getName().toLowerCase().contains(search)
                    || user.getUsername()
                    .toLowerCase()
                    .contains(search)
                    || user.getRole()
                    .toLowerCase()
                    .contains(search)
                    || user.getStatus()
                    .toLowerCase()
                    .contains(search)) {

                filteredUsers.add(user);
            }
        }

        usersTable.setItems(filteredUsers);
    }

    @FXML
    private void showAddUserDialog() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/admin/add-user.fxml"
                    )
            );

            Parent root = loader.load();

            AddUserController controller =
                    loader.getController();

            Stage dialog = new Stage();

            dialog.setTitle("Add User");

            dialog.initModality(
                    Modality.APPLICATION_MODAL
            );

            Scene scene = createStyledScene(root);

            dialog.setScene(scene);

            dialog.setResizable(false);

            dialog.showAndWait();

            if (controller.getCreatedName() != null) {

                UserRow newUser = new UserRow(
                        controller.getCreatedName(),
                        controller.getCreatedUsername(),
                        controller.getCreatedRole(),
                        "Active"
                );

                allUsers.add(newUser);

                filterUsers(searchField.getText());
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void handleEditUser(UserRow user) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/admin/edit-user.fxml"
                    )
            );

            Parent root = loader.load();

            EditUserController controller =
                    loader.getController();

            controller.setUser(user);

            Stage dialog = new Stage();

            dialog.setTitle("Edit User");

            dialog.initModality(
                    Modality.APPLICATION_MODAL
            );

            Scene scene = createStyledScene(root);

            dialog.setScene(scene);

            dialog.setResizable(false);

            dialog.showAndWait();

            usersTable.refresh();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     * Creates a Scene for Admin dialogs and applies
     * the same CSS used by the main application.
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

    private void handleDeactivateUser(UserRow user) {

        user.setStatus("Inactive");

        usersTable.refresh();
    }

    public static class UserRow {

        private final SimpleStringProperty name;

        private final SimpleStringProperty username;

        private final SimpleStringProperty role;

        private final SimpleStringProperty status;

        public UserRow(
                String name,
                String username,
                String role,
                String status
        ) {

            this.name =
                    new SimpleStringProperty(name);

            this.username =
                    new SimpleStringProperty(username);

            this.role =
                    new SimpleStringProperty(role);

            this.status =
                    new SimpleStringProperty(status);
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }

        public SimpleStringProperty usernameProperty() {
            return username;
        }

        public SimpleStringProperty roleProperty() {
            return role;
        }

        public SimpleStringProperty statusProperty() {
            return status;
        }

        public String getName() {
            return name.get();
        }

        public String getUsername() {
            return username.get();
        }

        public String getRole() {
            return role.get();
        }

        public String getStatus() {
            return status.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public void setUsername(String username) {
            this.username.set(username);
        }

        public void setRole(String role) {
            this.role.set(role);
        }

        public void setStatus(String status) {
            this.status.set(status);
        }
    }
}