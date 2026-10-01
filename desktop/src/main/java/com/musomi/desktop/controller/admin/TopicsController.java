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

public class TopicsController {

    @FXML
    private TableView<TopicRow> topicsTable;

    @FXML
    private TableColumn<TopicRow, String> nameColumn;

    @FXML
    private TableColumn<TopicRow, String> subjectColumn;

    @FXML
    private TableColumn<TopicRow, String> statusColumn;

    @FXML
    private TableColumn<TopicRow, String> actionsColumn;

    @FXML
    private TextField searchField;

    private final ObservableList<TopicRow> allTopics =
            FXCollections.observableArrayList();

    @FXML
    private void initialize() {

        nameColumn.setCellValueFactory(
                data -> data.getValue().nameProperty()
        );

        subjectColumn.setCellValueFactory(
                data -> data.getValue().subjectProperty()
        );

        statusColumn.setCellValueFactory(
                data -> data.getValue().statusProperty()
        );

        actionsColumn.setCellFactory(column ->
                new TableCell<TopicRow, String>() {

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

                            TopicRow topic =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            handleEditTopic(topic);
                        });

                        deactivateButton.setOnAction(event -> {

                            TopicRow topic =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            handleDeactivateTopic(topic);
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

        loadSampleTopics();

        topicsTable.setItems(allTopics);

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        filterTopics(newValue)
        );
    }

    private void loadSampleTopics() {

        allTopics.addAll(

                new TopicRow(
                        "Algebra",
                        "Mathematics",
                        "Active"
                ),

                new TopicRow(
                        "Geometry",
                        "Mathematics",
                        "Active"
                ),

                new TopicRow(
                        "Grammar",
                        "English",
                        "Active"
                ),

                new TopicRow(
                        "Forces",
                        "Physics",
                        "Inactive"
                )
        );
    }

    private void filterTopics(String searchText) {

        String search =
                searchText.toLowerCase().trim();

        if (search.isEmpty()) {

            topicsTable.setItems(allTopics);

            return;
        }

        ObservableList<TopicRow> filteredTopics =
                FXCollections.observableArrayList();

        for (TopicRow topic : allTopics) {

            if (topic.getName()
                    .toLowerCase()
                    .contains(search)

                    || topic.getSubject()
                    .toLowerCase()
                    .contains(search)

                    || topic.getStatus()
                    .toLowerCase()
                    .contains(search)) {

                filteredTopics.add(topic);
            }
        }

        topicsTable.setItems(filteredTopics);
    }

    @FXML
    private void showAddTopicDialog() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/admin/add-topic.fxml"
                    )
            );

            Parent root = loader.load();

            AddTopicController controller =
                    loader.getController();

            Stage dialog = new Stage();

            dialog.setTitle("Add Topic");

            dialog.initModality(
                    Modality.APPLICATION_MODAL
            );

            dialog.setScene(
                    new Scene(root)
            );

            dialog.setResizable(false);

            dialog.showAndWait();

            if (controller.getCreatedName() != null) {

                TopicRow newTopic =
                        new TopicRow(
                                controller.getCreatedName(),
                                controller.getCreatedSubject(),
                                "Active"
                        );

                allTopics.add(newTopic);

                filterTopics(
                        searchField.getText()
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void handleEditTopic(
            TopicRow topic
    ) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/admin/edit-topic.fxml"
                    )
            );

            Parent root = loader.load();

            EditTopicController controller =
                    loader.getController();

            controller.setTopicRow(topic);

            Stage dialog = new Stage();

            dialog.setTitle("Edit Topic");

            dialog.initModality(
                    Modality.APPLICATION_MODAL
            );

            dialog.setScene(
                    new Scene(root)
            );

            dialog.setResizable(false);

            dialog.showAndWait();

            topicsTable.refresh();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void handleDeactivateTopic(
            TopicRow topic
    ) {

        topic.setStatus("Inactive");

        topicsTable.refresh();
    }

    public static class TopicRow {

        private final SimpleStringProperty name;

        private final SimpleStringProperty subject;

        private final SimpleStringProperty status;

        public TopicRow(
                String name,
                String subject,
                String status
        ) {

            this.name =
                    new SimpleStringProperty(name);

            this.subject =
                    new SimpleStringProperty(subject);

            this.status =
                    new SimpleStringProperty(status);
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }

        public SimpleStringProperty subjectProperty() {
            return subject;
        }

        public SimpleStringProperty statusProperty() {
            return status;
        }

        public String getName() {
            return name.get();
        }

        public String getSubject() {
            return subject.get();
        }

        public String getStatus() {
            return status.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public void setSubject(String subject) {
            this.subject.set(subject);
        }

        public void setStatus(String status) {
            this.status.set(status);
        }
    }
}