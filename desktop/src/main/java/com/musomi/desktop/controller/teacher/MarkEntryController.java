package com.musomi.desktop.controller.teacher;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.musomi.desktop.controller.teacher.AssessmentStore.Assessment;
import com.musomi.desktop.controller.teacher.AssessmentStore.StudentMark;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

public class MarkEntryController {

    private static final Logger log = LoggerFactory.getLogger(MarkEntryController.class);
    private static final Pattern MARK_INPUT = Pattern.compile("\\d*(\\.\\d{0,2})?");

    @FXML private ComboBox<String> classComboBox;
    @FXML private ComboBox<String> subjectComboBox;
    @FXML private ComboBox<String> topicComboBox;
    @FXML private ComboBox<Assessment> assessmentComboBox;
    @FXML private TextField searchField;
    @FXML private TableView<MarkRow> marksTable;
    @FXML private TableColumn<MarkRow, String> numberColumn;
    @FXML private TableColumn<MarkRow, String> studentColumn;
    @FXML private TableColumn<MarkRow, String> markColumn;
    @FXML private TableColumn<MarkRow, String> totalColumn;
    @FXML private TableColumn<MarkRow, String> gradeColumn;
    @FXML private TableColumn<MarkRow, String> remarkColumn;
    @FXML private Label progressLabel;
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Button saveAllButton;

    private final ObservableList<MarkRow> allRows = FXCollections.observableArrayList();
    private FilteredList<MarkRow> filteredRows;
    private Assessment selectedAssessment;
    private boolean updatingFilters;

    @FXML
    private void initialize() {
        setupTable();
        filteredRows = new FilteredList<>(allRows, row -> true);
        marksTable.setItems(filteredRows);
        setupSearch();

        List<Assessment> assessments = AssessmentStore.getAssessments();
        Assessment initial = assessments.stream()
                .filter(AssessmentStore::isPending)
                .findFirst()
                .orElseGet(() -> assessments.stream().findFirst().orElse(null));
        if (initial == null) {
            clearSelection("No assessments are available yet.");
            return;
        }

        classComboBox.setItems(options(assessments.stream()
                .map(Assessment::getClassName).collect(Collectors.toSet())));
        classComboBox.setValue(initial.getClassName());
        subjectComboBox.setItems(subjectOptions(initial.getClassName()));
        subjectComboBox.setValue(initial.getSubject());
        topicComboBox.setItems(topicOptions(initial.getClassName(), initial.getSubject()));
        topicComboBox.setValue(initial.getTopic());
        setupFilters();
        setupKeyboardNavigation();
        refreshAssessmentChoices(initial);
    }

    private void setupFilters() {
        assessmentComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Assessment assessment) {
                return assessment == null ? "" : assessmentLabel(assessment);
            }

            @Override
            public Assessment fromString(String value) {
                return null;
            }
        });
        assessmentComboBox.setPrefWidth(320);
        assessmentComboBox.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Assessment assessment, boolean empty) {
                super.updateItem(assessment, empty);
                setText(empty || assessment == null ? null : assessmentLabel(assessment));
                setTooltip(empty || assessment == null
                        ? null : new javafx.scene.control.Tooltip(assessmentLabel(assessment)));
            }
        });
        assessmentComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Assessment assessment, boolean empty) {
                super.updateItem(assessment, empty);
                setText(empty || assessment == null ? null : assessmentLabel(assessment));
                setTooltip(empty || assessment == null
                        ? null : new javafx.scene.control.Tooltip(assessmentLabel(assessment)));
            }
        });

        classComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!updatingFilters) {
                updateSubjectAndTopicOptions();
                refreshAssessmentChoices(null);
            }
        });
        subjectComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!updatingFilters) {
                updateTopicOptions();
                refreshAssessmentChoices(null);
            }
        });
        topicComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!updatingFilters) {
                refreshAssessmentChoices(null);
            }
        });
        assessmentComboBox.valueProperty().addListener((obs, oldValue, newValue) -> showAssessment(newValue));
    }

    private ObservableList<String> options(Set<String> values) {
        return FXCollections.observableArrayList(new TreeSet<>(values));
    }

    private ObservableList<String> subjectOptions(String className) {
        return options(AssessmentStore.getAssessments().stream()
                .filter(assessment -> className == null || className.equals(assessment.getClassName()))
                .map(Assessment::getSubject)
                .collect(Collectors.toSet()));
    }

    private ObservableList<String> topicOptions(String className, String subject) {
        return options(AssessmentStore.getAssessments().stream()
                .filter(assessment -> className == null || className.equals(assessment.getClassName()))
                .filter(assessment -> subject == null || subject.equals(assessment.getSubject()))
                .map(Assessment::getTopic)
                .collect(Collectors.toSet()));
    }

    private void updateSubjectAndTopicOptions() {
        updatingFilters = true;
        try {
            ObservableList<String> subjects = subjectOptions(classComboBox.getValue());
            subjectComboBox.setItems(subjects);
            if (!subjects.contains(subjectComboBox.getValue())) {
                subjectComboBox.setValue(null);
            }

            ObservableList<String> topics = topicOptions(classComboBox.getValue(), subjectComboBox.getValue());
            topicComboBox.setItems(topics);
            if (!topics.contains(topicComboBox.getValue())) {
                topicComboBox.setValue(null);
            }
        } finally {
            updatingFilters = false;
        }
    }

    private void updateTopicOptions() {
        updatingFilters = true;
        try {
            ObservableList<String> topics = topicOptions(
                    classComboBox.getValue(), subjectComboBox.getValue());
            topicComboBox.setItems(topics);
            if (!topics.contains(topicComboBox.getValue())) {
                topicComboBox.setValue(null);
            }
        } finally {
            updatingFilters = false;
        }
    }

    private String assessmentLabel(Assessment assessment) {
        return assessment.getName() + " — " + assessment.getClassName()
                + " — " + assessment.getSubject();
    }

    private void refreshAssessmentChoices(Assessment preferred) {
        String className = classComboBox.getValue();
        String subject = subjectComboBox.getValue();
        String topic = topicComboBox.getValue();

        List<Assessment> matches = AssessmentStore.getAssessments().stream()
                .filter(assessment -> className == null || className.equals(assessment.getClassName()))
                .filter(assessment -> subject == null || subject.equals(assessment.getSubject()))
                .filter(assessment -> topic == null || topic.equals(assessment.getTopic()))
                .toList();

        assessmentComboBox.setItems(FXCollections.observableArrayList(matches));
        Assessment next = preferred != null && matches.contains(preferred)
                ? preferred
                : matches.stream()
                        .filter(AssessmentStore::isPending)
                        .findFirst()
                        .orElseGet(() -> matches.stream().findFirst().orElse(null));
        assessmentComboBox.setValue(next);
    }

    /** Receives an assessment selected from another view, such as the dashboard reminder. */
    public void setData(Object data) {
        if (data instanceof Assessment assessment
                && AssessmentStore.getAssessments().contains(assessment)) {
            classComboBox.setValue(assessment.getClassName());
            subjectComboBox.setValue(assessment.getSubject());
            topicComboBox.setValue(assessment.getTopic());
            refreshAssessmentChoices(assessment);
        }
    }

    private void showAssessment(Assessment assessment) {
        selectedAssessment = assessment;
        allRows.clear();

        if (assessment == null) {
            clearSelection("No assessment matches the selected filters.");
            return;
        }

        List<StudentMark> marks = AssessmentStore.getMarks(assessment);
        for (StudentMark mark : marks) {
            allRows.add(new MarkRow(mark, assessment.getMaxMarks()));
        }

        boolean editable = "Draft".equals(assessment.getStatus());
        marksTable.setEditable(editable);
        saveAllButton.setDisable(!editable);
        statusLabel.setText(editable
                ? "Draft marks are saved in this session when each cell is committed."
                : "Published assessments are read-only.");
        marksTable.setPlaceholder(new Label(marks.isEmpty()
                ? "No students are enrolled in this class."
                : "No students match your search."));
        updateProgress();
    }

    private void clearSelection(String message) {
        selectedAssessment = null;
        allRows.clear();
        marksTable.setEditable(false);
        marksTable.setPlaceholder(new Label(message));
        saveAllButton.setDisable(true);
        statusLabel.setText(message);
        updateProgress();
    }

    private void setupTable() {
        numberColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNumber()));
        studentColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStudent()));
        markColumn.setCellValueFactory(data -> data.getValue().markProperty());
        totalColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getMaxMarks())));
        gradeColumn.setCellValueFactory(data -> data.getValue().gradeProperty());
        remarkColumn.setCellValueFactory(data -> data.getValue().remarkProperty());

        markColumn.setCellFactory(column -> new MarkCell());
        gradeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String grade, boolean empty) {
                super.updateItem(grade, empty);
                getStyleClass().removeIf(style -> style.matches("grade-[a-f]"));
                if (empty || grade == null || grade.isBlank()) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(grade);
                    setGraphic(null);
                    getStyleClass().add("grade-" + grade.toLowerCase());
                }
            }
        });
        marksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        marksTable.setPlaceholder(new Label("Select an assessment to enter marks."));
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String query = newValue == null ? "" : newValue.trim().toLowerCase();
            filteredRows.setPredicate(row -> query.isEmpty()
                    || row.getStudent().toLowerCase().contains(query)
                    || row.getNumber().toLowerCase().contains(query));
        });
    }

    private void setupKeyboardNavigation() {
        marksTable.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() != KeyCode.ENTER && event.getCode() != KeyCode.TAB) {
                return;
            }

            if (marksTable.getEditingCell() != null) {
                return;
            }

            int row = marksTable.getSelectionModel().getSelectedIndex();
            if (row < 0 && !filteredRows.isEmpty()) {
                row = 0;
            }
            if (row >= 0 && row < filteredRows.size()) {
                event.consume();
                startEditing(row);
            }
        });
    }

    private void startEditing(int row) {
        if (selectedAssessment == null || !"Draft".equals(selectedAssessment.getStatus())
                || row < 0 || row >= filteredRows.size()) {
            return;
        }

        marksTable.getSelectionModel().select(row);
        marksTable.getFocusModel().focus(row, markColumn);
        marksTable.scrollTo(row);
        Platform.runLater(() -> marksTable.edit(row, markColumn));
    }

    private void moveToNextStudent(int currentIndex) {
        int nextIndex = currentIndex + 1;
        if (nextIndex < filteredRows.size()) {
            Platform.runLater(() -> startEditing(nextIndex));
        } else {
            statusLabel.setText("Last visible student updated. Use the arrow keys to review the table.");
        }
    }

    private void updateProgress() {
        int total = allRows.size();
        int entered = (int) allRows.stream()
                .filter(row -> StudentMark.hasValidMark(row.getMark(), row.getMaxMarks()))
                .count();
        progressLabel.setText(entered + " / " + total + " students marked");
        progressBar.setProgress(total == 0 ? 0 : (double) entered / total);
    }

    @FXML
    private void saveAll() {
        if (selectedAssessment == null) {
            return;
        }
        statusLabel.setText("All committed marks are saved for this session. Server sync is not available yet.");
        log.debug("Confirmed in-session marks for assessment '{}'", selectedAssessment.getName());
    }

    private void showValidationMessage(String message) {
        statusLabel.setText(message);
        log.debug("Mark entry validation: {}", message);
    }

    public static class MarkRow {
        private final StudentMark studentMark;
        private final int maxMarks;
        private final StringProperty mark = new SimpleStringProperty();
        private final StringProperty grade = new SimpleStringProperty();
        private final StringProperty remark = new SimpleStringProperty();

        private MarkRow(StudentMark studentMark, int maxMarks) {
            this.studentMark = studentMark;
            this.maxMarks = maxMarks;
            mark.set(studentMark.getMark());
            updatePreview(studentMark.getMark());
        }

        public String getNumber() {
            return studentMark.getNumber();
        }

        public String getStudent() {
            return studentMark.getStudent();
        }

        public String getMark() {
            return studentMark.getMark();
        }

        public int getMaxMarks() {
            return maxMarks;
        }

        public StringProperty markProperty() {
            return mark;
        }

        public StringProperty gradeProperty() {
            return grade;
        }

        public StringProperty remarkProperty() {
            return remark;
        }

        private void updatePreview(String value) {
            String letterGrade = StudentMark.gradeFor(value, maxMarks);
            grade.set(letterGrade);
            remark.set(StudentMark.remarkFor(letterGrade));
        }

        private void save(String value) {
            String normalized = value == null ? "" : value.trim();
            studentMark.setMark(normalized);
            mark.set(normalized);
            updatePreview(normalized);
        }
    }

    private final class MarkCell extends TableCell<MarkRow, String> {
        private final TextField editor = new TextField();
        private String originalValue;

        private MarkCell() {
            getStyleClass().add("mark-score-cell");
            editor.getStyleClass().add("mark-score-editor");
            editor.setTextFormatter(new javafx.scene.control.TextFormatter<String>(change ->
                    MARK_INPUT.matcher(change.getControlNewText()).matches() ? change : null));
            editor.textProperty().addListener((observable, oldValue, newValue) -> {
                if (isEditing() && getTableRow() != null && getTableRow().getItem() != null) {
                    getTableRow().getItem().updatePreview(newValue);
                    statusLabel.setText("Editing mark for " + getTableRow().getItem().getStudent());
                }
            });
            editor.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                    event.consume();
                } else if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB) {
                    int currentIndex = getIndex();
                    if (commitValue(true)) {
                        event.consume();
                        moveToNextStudent(currentIndex);
                    } else {
                        event.consume();
                    }
                }
            });
            editor.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
                if (wasFocused && !isFocused && isEditing() && !commitValue(false)) {
                    cancelEdit();
                }
            });
        }

        @Override
        public void startEdit() {
            if (isEmpty() || selectedAssessment == null
                    || !"Draft".equals(selectedAssessment.getStatus())) {
                return;
            }
            super.startEdit();
            originalValue = getItem() == null ? "" : getItem();
            editor.setText(originalValue);
            setText(null);
            setGraphic(editor);
            editor.requestFocus();
            editor.selectAll();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            if (getTableRow() != null && getTableRow().getItem() != null) {
                getTableRow().getItem().updatePreview(originalValue);
            }
            setGraphic(null);
            setText(getItem());
        }

        @Override
        protected void updateItem(String value, boolean empty) {
            super.updateItem(value, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else if (isEditing()) {
                setText(null);
                setGraphic(editor);
            } else {
                setText(value);
                setGraphic(null);
            }
        }

        private boolean commitValue(boolean keepEditingOnFailure) {
            String value = editor.getText() == null ? "" : editor.getText().trim();
            MarkRow row = getTableRow() == null ? null : getTableRow().getItem();
            if (row == null) {
                cancelEdit();
                return false;
            }
            if (!value.isEmpty()) {
                try {
                    java.math.BigDecimal score = new java.math.BigDecimal(value);
                    if (score.signum() < 0 || score.compareTo(java.math.BigDecimal.valueOf(row.getMaxMarks())) > 0) {
                        showValidationMessage("Enter a mark from 0 to " + row.getMaxMarks() + ".");
                        if (keepEditingOnFailure) {
                            editor.requestFocus();
                            editor.selectAll();
                        }
                        return false;
                    }
                } catch (NumberFormatException ex) {
                    showValidationMessage("Enter a valid numeric mark.");
                    if (keepEditingOnFailure) {
                        editor.requestFocus();
                        editor.selectAll();
                    }
                    return false;
                }
            }

            int rowIndex = getIndex();
            row.save(value);
            commitEdit(value);
            updateProgress();
            statusLabel.setText("Saved for this session: " + row.getStudent());
            log.debug("Saved mark for student '{}' in assessment '{}'",
                    row.getStudent(), selectedAssessment.getName());
            marksTable.getSelectionModel().select(rowIndex);
            marksTable.getFocusModel().focus(rowIndex, markColumn);
            return true;
        }
    }
}
