package com.musomi.desktop.controller.teacher;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

public class MarkEntryController {

    @FXML
    private ComboBox<String> classComboBox;

    @FXML
    private ComboBox<String> subjectComboBox;

    @FXML
    private ComboBox<String> topicComboBox;

    @FXML
    private ComboBox<String> assessmentComboBox;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<MarkRow> marksTable;

    @FXML
    private TableColumn<MarkRow, String> numberColumn;

    @FXML
    private TableColumn<MarkRow, String> studentColumn;

    @FXML
    private TableColumn<MarkRow, String> markColumn;

    @FXML
    private TableColumn<MarkRow, String> totalColumn;

    @FXML
    private TableColumn<MarkRow, String> gradeColumn;

    @FXML
    private TableColumn<MarkRow, String> remarkColumn;

    @FXML
    private Label progressLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button saveAllButton;

    private final ObservableList<MarkRow> allRows =
            FXCollections.observableArrayList();

    private FilteredList<MarkRow> filteredRows;

    private static final double MAX_MARK = 40.0;

    @FXML
    private void initialize() {

        setupDropdowns();
        setupMockStudents();
        setupTable();
        setupSearch();
        setupKeyboardNavigation();

        updateProgress();

        statusLabel.setText("Ready");
    }

    private void setupDropdowns() {

        classComboBox.setItems(FXCollections.observableArrayList(
                "S3 Blue",
                "S3 Red",
                "S4 Blue",
                "S4 Red",
                "S5"
        ));

        subjectComboBox.setItems(FXCollections.observableArrayList(
                "Mathematics",
                "English",
                "Biology",
                "Chemistry"
        ));

        topicComboBox.setItems(FXCollections.observableArrayList(
                "Algebra",
                "Geometry",
                "Statistics",
                "Functions"
        ));

        assessmentComboBox.setItems(FXCollections.observableArrayList(
                "Test 1",
                "Test 2",
                "Assignment",
                "Quiz"
        ));

        classComboBox.getSelectionModel().selectFirst();
        subjectComboBox.getSelectionModel().selectFirst();
        topicComboBox.getSelectionModel().selectFirst();
        assessmentComboBox.getSelectionModel().selectFirst();
    }

    private void setupMockStudents() {

        allRows.clear();

        String[] students = {
                "John Mugisha",
                "Sarah Namukasa",
                "Brian Okello",
                "Mary Achieng",
                "David Kato",
                "Grace Nakato",
                "Daniel Ouma",
                "Rebecca Atim",
                "Peter Ssekandi",
                "Esther Nankya",
                "Samuel Tumusiime",
                "Angela Nabirye"
        };

        double[] marks = {
                35,
                32,
                28,
                30,
                24,
                38,
                19,
                34,
                27,
                31,
                22,
                36
        };

        for (int i = 0; i < students.length; i++) {

            allRows.add(
                    new MarkRow(
                            String.valueOf(i + 1),
                            students[i],
                            String.valueOf((int) marks[i])
                    )
            );
        }

        filteredRows = new FilteredList<>(allRows, row -> true);
    }

    private void setupTable() {

        numberColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getNumber()
                )
        );

        studentColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getStudent()
                )
        );

        markColumn.setCellValueFactory(
                data -> data.getValue().markProperty()
        );

        totalColumn.setCellValueFactory(
                data -> new SimpleStringProperty("40")
        );

        gradeColumn.setCellValueFactory(
                data -> data.getValue().gradeProperty()
        );

        remarkColumn.setCellValueFactory(
                data -> data.getValue().remarkProperty()
        );

        setupEditableMarkColumn();

        marksTable.setItems(filteredRows);

        marksTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
    }

    private void setupEditableMarkColumn() {

        markColumn.setCellFactory(
                new Callback<TableColumn<MarkRow, String>,
                        TableCell<MarkRow, String>>() {

                    @Override
                    public TableCell<MarkRow, String> call(
                            TableColumn<MarkRow, String> column) {

                        return new TableCell<>() {

                            private final TextField textField =
                                    new TextField();

                            {
                                textField.setMaxWidth(
                                        Double.MAX_VALUE
                                );

                                textField.setOnKeyPressed(
                                        this::handleMarkKeyPressed
                                );

                                textField.focusedProperty().addListener(
                                        (obs, wasFocused, isFocused) -> {

                                            if (isFocused) {
                                                textField.selectAll();
                                            }
                                        }
                                );

                                textField.textProperty().addListener(
                                        (obs, oldValue, newValue) -> {

                                            if (newValue != null &&
                                                    !newValue.matches(
                                                            "\\d*(\\.\\d*)?"
                                                    )) {

                                                textField.setText(oldValue);
                                            }
                                        }
                                );
                            }

                            @Override
                            public void startEdit() {

                                if (!isEmpty()) {

                                    super.startEdit();

                                    MarkRow row =
                                            getTableView()
                                                    .getItems()
                                                    .get(getIndex());

                                    textField.setText(
                                            row.getMark()
                                    );

                                    setGraphic(textField);
                                    setText(null);

                                    textField.requestFocus();
                                    textField.selectAll();
                                }
                            }

                            @Override
                            public void cancelEdit() {

                                super.cancelEdit();

                                setText(getItem());
                                setGraphic(null);
                            }

                            @Override
                            public void commitEdit(
                                    String newValue) {

                                MarkRow row =
                                        getTableView()
                                                .getItems()
                                                .get(getIndex());

                                if (!isValidMark(newValue)) {

                                    showInvalidMarkMessage();

                                    textField.setText(
                                            row.getMark()
                                    );

                                    textField.requestFocus();
                                    textField.selectAll();

                                    return;
                                }

                                super.commitEdit(newValue);

                                row.setMark(newValue);

                                updateProgress();

                                statusLabel.setText(
                                        "Mark entered for "
                                                + row.getStudent()
                                );
                            }

                            private void handleMarkKeyPressed(
                                    KeyEvent event) {

                                if (event.getCode() == KeyCode.ENTER ||
                                        event.getCode() == KeyCode.TAB) {

                                    event.consume();

                                    commitAndMoveToNext();
                                }
                            }

                            private void commitAndMoveToNext() {

                                String value =
                                        textField.getText();

                                if (!isValidMark(value)) {

                                    showInvalidMarkMessage();

                                    textField.requestFocus();
                                    textField.selectAll();

                                    return;
                                }

                                int currentIndex = getIndex();

                                MarkRow currentRow =
                                        getTableView()
                                                .getItems()
                                                .get(currentIndex);

                                currentRow.setMark(value);

                                updateProgress();

                                commitEdit(value);

                                moveToNextStudent(currentIndex);
                            }

                            private boolean isValidMark(
                                    String value) {

                                if (value == null ||
                                        value.trim().isEmpty()) {

                                    return false;
                                }

                                try {

                                    double mark =
                                            Double.parseDouble(
                                                    value.trim()
                                            );

                                    return mark >= 0 &&
                                            mark <= MAX_MARK;

                                } catch (NumberFormatException e) {

                                    return false;
                                }
                            }
                        };
                    }
                }
        );
    }

    private void setupKeyboardNavigation() {

        marksTable.addEventFilter(
                KeyEvent.KEY_PRESSED,
                event -> {

                    if (event.getCode() != KeyCode.ENTER &&
                            event.getCode() != KeyCode.TAB) {

                        return;
                    }

                    TablePosition<?, ?> position =
                            marksTable
                                    .getFocusModel()
                                    .getFocusedCell();

                    if (position == null) {
                        return;
                    }

                    int rowIndex = position.getRow();

                    if (rowIndex < 0 ||
                            rowIndex >= marksTable
                                    .getItems()
                                    .size()) {

                        return;
                    }

                    if (position.getTableColumn() == markColumn) {
                        return;
                    }

                    event.consume();

                    startEditingRow(rowIndex);
                }
        );
    }

    private void startEditingRow(int rowIndex) {

        if (rowIndex < 0 ||
                rowIndex >= marksTable.getItems().size()) {

            return;
        }

        marksTable.getSelectionModel()
                .select(rowIndex);

        marksTable.getFocusModel()
                .focus(rowIndex, markColumn);

        marksTable.scrollTo(rowIndex);

        Platform.runLater(() ->
                marksTable.edit(rowIndex, markColumn)
        );
    }

    private void moveToNextStudent(int currentIndex) {

        int nextIndex = currentIndex + 1;

        if (nextIndex >= marksTable.getItems().size()) {

            statusLabel.setText(
                    "All visible student marks entered."
            );

            marksTable.getSelectionModel()
                    .clearSelection();

            return;
        }

        Platform.runLater(() -> {

            marksTable.getSelectionModel()
                    .select(nextIndex);

            marksTable.getFocusModel()
                    .focus(nextIndex, markColumn);

            marksTable.scrollTo(nextIndex);

            marksTable.edit(
                    nextIndex,
                    markColumn
            );
        });
    }

    private boolean isValidMark(String value) {

        if (value == null ||
                value.trim().isEmpty()) {

            return false;
        }

        try {

            double mark =
                    Double.parseDouble(value.trim());

            return mark >= 0 &&
                    mark <= MAX_MARK;

        } catch (NumberFormatException e) {

            return false;
        }
    }

    private void showInvalidMarkMessage() {

        Alert alert =
                new Alert(Alert.AlertType.WARNING);

        alert.setTitle("Invalid Mark");

        alert.setHeaderText(
                "Invalid mark entered"
        );

        alert.setContentText(
                "Mark must be between 0 and 40."
        );

        alert.showAndWait();
    }

    private void setupSearch() {

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> {

                    String search =
                            newValue == null
                                    ? ""
                                    : newValue
                                    .trim()
                                    .toLowerCase();

                    filteredRows.setPredicate(row -> {

                        if (search.isEmpty()) {
                            return true;
                        }

                        return row.getStudent()
                                .toLowerCase()
                                .contains(search);
                    });

                    updateProgress();
                }
        );
    }

    @FXML
    private void saveAll() {

        updateProgress();

        statusLabel.setText(
                "All marks saved locally."
        );

        saveAllButton.setText(
                "Saved ✓"
        );

        Platform.runLater(() ->
                saveAllButton.setText("Save All")
        );
    }

    private void updateProgress() {

        if (filteredRows == null ||
                filteredRows.isEmpty()) {

            progressLabel.setText("0 / 0 students");

            return;
        }

        int completed = 0;

        for (MarkRow row : filteredRows) {

            if (isValidMark(row.getMark())) {
                completed++;
            }
        }

        progressLabel.setText(
                completed +
                        " / " +
                        filteredRows.size() +
                        " students"
        );
    }

    public static class MarkRow {

        private final SimpleStringProperty number;
        private final SimpleStringProperty student;
        private final SimpleStringProperty mark;
        private final SimpleStringProperty grade;
        private final SimpleStringProperty remark;

        public MarkRow(
                String number,
                String student,
                String mark) {

            this.number =
                    new SimpleStringProperty(number);

            this.student =
                    new SimpleStringProperty(student);

            this.mark =
                    new SimpleStringProperty(mark);

            this.grade =
                    new SimpleStringProperty("");

            this.remark =
                    new SimpleStringProperty("");

            updateGradeAndRemark();
        }

        public String getNumber() {
            return number.get();
        }

        public String getStudent() {
            return student.get();
        }

        public String getMark() {
            return mark.get();
        }

        public void setMark(String value) {

            mark.set(value);

            updateGradeAndRemark();
        }

        public SimpleStringProperty markProperty() {
            return mark;
        }

        public SimpleStringProperty gradeProperty() {
            return grade;
        }

        public SimpleStringProperty remarkProperty() {
            return remark;
        }

        private void updateGradeAndRemark() {

            if (mark.get() == null ||
                    mark.get().trim().isEmpty()) {

                grade.set("");
                remark.set("");

                return;
            }

            try {

                double value =
                        Double.parseDouble(
                                mark.get()
                        );

                double percentage =
                        (value / 40.0) * 100.0;

                if (percentage >= 80) {

                    grade.set("A");
                    remark.set("Excellent");

                } else if (percentage >= 70) {

                    grade.set("B");
                    remark.set("Very Good");

                } else if (percentage >= 60) {

                    grade.set("C");
                    remark.set("Good");

                } else if (percentage >= 50) {

                    grade.set("D");
                    remark.set("Satisfactory");

                } else if (percentage >= 40) {

                    grade.set("E");
                    remark.set(
                            "Needs Improvement"
                    );

                } else {

                    grade.set("F");
                    remark.set("Fail");
                }

            } catch (NumberFormatException e) {

                grade.set("");
                remark.set("");
            }
        }
    }
}