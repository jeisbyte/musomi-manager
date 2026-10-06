package com.musomi.desktop.controller.teacher;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Controller for /fxml/teacher/assessments.fxml.
 *
 * All data is local/mock — replace with AssessmentService calls once the
 * backend endpoint is ready. The mock list is intentionally isolated in the
 * MOCK_DATA field so it is easy to swap.
 */
public class AssessmentsController {

    // =========================================================================
    // Inner model
    // =========================================================================

    public static class Assessment {
        private String  name;
        private String  className;
        private String  subject;
        private String  topic;
        private String  date;       // formatted for display
        private int     maxMarks;
        private String  status;     // "Draft" | "Published"

        public Assessment(String name, String className, String subject,
                          String topic, String date, int maxMarks, String status) {
            this.name      = name;
            this.className = className;
            this.subject   = subject;
            this.topic     = topic;
            this.date      = date;
            this.maxMarks  = maxMarks;
            this.status    = status;
        }

        public String getName()      { return name; }
        public String getClassName() { return className; }
        public String getSubject()   { return subject; }
        public String getTopic()     { return topic; }
        public String getDate()      { return date; }
        public int    getMaxMarks()  { return maxMarks; }
        public String getStatus()    { return status; }

        public void setName(String n)       { this.name = n; }
        public void setClassName(String c)  { this.className = c; }
        public void setSubject(String s)    { this.subject = s; }
        public void setTopic(String t)      { this.topic = t; }
        public void setDate(String d)       { this.date = d; }
        public void setMaxMarks(int m)      { this.maxMarks = m; }
        public void setStatus(String st)    { this.status = st; }
    }

    // =========================================================================
    // Mock data — isolated for backend swap-out
    // =========================================================================

    /** Mutable list — new assessments are added here at runtime. */
    private final List<Assessment> assessments = new ArrayList<>(List.of(
        new Assessment("Mathematics Test 1", "S3 Blue", "Mathematics",
                       "Algebra",      "15 Sep 2026", 40, "Draft"),
        new Assessment("Mathematics Test 2", "S3 Red",  "Mathematics",
                       "Geometry",     "20 Sep 2026", 40, "Published"),
        new Assessment("Biology Assignment", "S4 Blue", "Biology",
                       "Cell Biology", "25 Sep 2026", 30, "Draft")
    ));

    // Mock combo options — replace with API data later
    private static final List<String> CLASS_OPTIONS   = List.of(
            "S3 Blue", "S3 Red", "S4 Blue", "S4 Red", "S5");
    private static final List<String> SUBJECT_OPTIONS = List.of(
            "Mathematics", "Biology", "Chemistry", "Physics",
            "English", "History", "Geography");
    private static final List<String> TOPIC_OPTIONS   = List.of(
            "Algebra", "Geometry", "Calculus", "Statistics",
            "Cell Biology", "Genetics", "Ecology",
            "Acids & Bases", "Organic Chemistry",
            "Mechanics", "Electrostatics",
            "Grammar", "Literature",
            "World War II", "African History",
            "Map Reading", "Climate");

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    // =========================================================================
    // FXML bindings — list view
    // =========================================================================

    @FXML private Button  createBtn;
    @FXML private VBox    listPane;
    @FXML private VBox    rowsContainer;
    @FXML private VBox    emptyState;

    // =========================================================================
    // FXML bindings — form overlay
    // =========================================================================

    @FXML private VBox       formPane;
    @FXML private Label      formTitle;
    @FXML private Label      errorLabel;
    @FXML private Label      successLabel;
    @FXML private TextField  nameField;
    @FXML private ComboBox<String> classCombo;
    @FXML private ComboBox<String> subjectCombo;
    @FXML private ComboBox<String> topicCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextField  marksField;
    @FXML private Button     saveDraftBtn;

    /** When editing an existing assessment, this holds the target. */
    private Assessment editingAssessment = null;

    // =========================================================================
    // Lifecycle
    // =========================================================================

    @FXML
    private void initialize() {
        classCombo.getItems().addAll(CLASS_OPTIONS);
        subjectCombo.getItems().addAll(SUBJECT_OPTIONS);
        topicCombo.getItems().addAll(TOPIC_OPTIONS);
        refreshList();
    }

    // =========================================================================
    // List rendering
    // =========================================================================

    private void refreshList() {
        rowsContainer.getChildren().clear();

        boolean hasRows = !assessments.isEmpty();
        emptyState.setVisible(!hasRows);
        emptyState.setManaged(!hasRows);

        for (int i = 0; i < assessments.size(); i++) {
            Assessment a = assessments.get(i);
            boolean isEven = (i % 2 == 0);
            rowsContainer.getChildren().add(buildRow(a, isEven));
            if (i < assessments.size() - 1) {
                Separator sep = new Separator();
                sep.setStyle("-fx-background-color: #F1F5F9;");
                rowsContainer.getChildren().add(sep);
            }
        }
    }

    private HBox buildRow(Assessment a, boolean isEven) {
        String rowBg = isEven ? "white" : "#FAFBFC";

        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 20, 14, 20));
        row.setStyle("-fx-background-color: " + rowBg + ";");

        // Name
        VBox nameCell = new VBox(2);
        nameCell.setMinWidth(200);
        nameCell.setPrefWidth(200);
        Label nameLabel = new Label(a.getName());
        nameLabel.setStyle("-fx-font-size:13px;-fx-font-weight:600;-fx-text-fill:#0F172A;");
        nameCell.getChildren().add(nameLabel);

        // Class
        Label clsLabel = cell(a.getClassName(), 100);

        // Subject
        Label subLabel = cell(a.getSubject(), 130);

        // Topic
        Label topLabel = cell(a.getTopic(), 150);

        // Date
        Label dateLabel = cell(a.getDate(), 110);

        // Max Marks
        Label marksLabel = cell(String.valueOf(a.getMaxMarks()), 90);

        // Status badge
        HBox statusCell = new HBox();
        statusCell.setMinWidth(100);
        statusCell.setPrefWidth(100);
        statusCell.setAlignment(Pos.CENTER_LEFT);

        Label statusBadge = new Label(a.getStatus());
        if ("Published".equals(a.getStatus())) {
            statusBadge.setStyle(
                "-fx-background-color: #ECFDF5;" +
                "-fx-text-fill: #047857;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 600;" +
                "-fx-padding: 3 10;" +
                "-fx-background-radius: 999px;");
        } else {
            statusBadge.setStyle(
                "-fx-background-color: #FFF7ED;" +
                "-fx-text-fill: #B45309;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 600;" +
                "-fx-padding: 3 10;" +
                "-fx-background-radius: 999px;");
        }
        statusCell.getChildren().add(statusBadge);

        // Action cell
        HBox actionCell = new HBox(6);
        actionCell.setMinWidth(80);
        actionCell.setAlignment(Pos.CENTER_LEFT);

        if ("Draft".equals(a.getStatus())) {
            Button editBtn = new Button("Edit");
            editBtn.setStyle(
                "-fx-background-color: #EEF2FF;" +
                "-fx-text-fill: #4F46E5;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 600;" +
                "-fx-background-radius: 6px;" +
                "-fx-padding: 5 12;" +
                "-fx-cursor: hand;");
            editBtn.setOnAction(e -> openEditForm(a));
            actionCell.getChildren().add(editBtn);
        } else {
            Label publishedTag = new Label("Published");
            publishedTag.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #94A3B8;");
            actionCell.getChildren().add(publishedTag);
        }

        row.getChildren().addAll(
                nameCell, clsLabel, subLabel, topLabel,
                dateLabel, marksLabel, statusCell, actionCell);
        return row;
    }

    private Label cell(String text, double minWidth) {
        Label lbl = new Label(text);
        lbl.setMinWidth(minWidth);
        lbl.setPrefWidth(minWidth);
        lbl.setStyle("-fx-font-size:13px;-fx-text-fill:#475569;");
        return lbl;
    }

    // =========================================================================
    // Form — open / close
    // =========================================================================

    @FXML
    private void handleCreateAssessment() {
        editingAssessment = null;
        formTitle.setText("Create Assessment");
        saveDraftBtn.setText("Save Draft");
        clearForm();
        showForm(true);
    }

    private void openEditForm(Assessment a) {
        editingAssessment = a;
        formTitle.setText("Edit Assessment");
        saveDraftBtn.setText("Save Changes");
        populateForm(a);
        showForm(true);
    }

    @FXML
    private void handleCancelForm() {
        showForm(false);
    }

    private void showForm(boolean visible) {
        formPane.setVisible(visible);
        formPane.setManaged(visible);
        // Prevent list interaction while form is open
        listPane.setDisable(visible);
        createBtn.setDisable(visible);
        hideMessages();
    }

    // =========================================================================
    // Form — populate / clear
    // =========================================================================

    private void clearForm() {
        nameField.clear();
        classCombo.setValue(null);
        subjectCombo.setValue(null);
        topicCombo.setValue(null);
        datePicker.setValue(null);
        marksField.clear();
        hideMessages();
    }

    private void populateForm(Assessment a) {
        nameField.setText(a.getName());
        classCombo.setValue(a.getClassName());
        subjectCombo.setValue(a.getSubject());
        topicCombo.setValue(a.getTopic());
        marksField.setText(String.valueOf(a.getMaxMarks()));
        datePicker.setValue(null); // date stored as string, leave blank for re-selection
        hideMessages();
    }

    // =========================================================================
    // Form — save draft
    // =========================================================================

    @FXML
    private void handleSaveDraft() {
        hideMessages();

        // ── Validation ────────────────────────────────────────────────────────
        String name    = nameField.getText() == null ? "" : nameField.getText().trim();
        String cls     = classCombo.getValue()   == null ? "" : classCombo.getValue().trim();
        String subject = subjectCombo.getValue() == null ? "" : subjectCombo.getValue().trim();
        String topic   = topicCombo.getValue()   == null ? "" : topicCombo.getValue().trim();
        LocalDate date = datePicker.getValue();
        String marksStr= marksField.getText()    == null ? "" : marksField.getText().trim();

        if (name.isEmpty()) {
            showError("Assessment Name is required.");
            return;
        }
        if (cls.isEmpty()) {
            showError("Class is required.");
            return;
        }
        if (subject.isEmpty()) {
            showError("Subject is required.");
            return;
        }
        if (topic.isEmpty()) {
            showError("Topic is required.");
            return;
        }
        if (date == null) {
            showError("Assessment Date is required.");
            return;
        }
        if (marksStr.isEmpty()) {
            showError("Maximum Marks is required.");
            return;
        }

        int maxMarks;
        try {
            maxMarks = Integer.parseInt(marksStr);
        } catch (NumberFormatException ex) {
            showError("Maximum Marks must be a whole number (e.g. 40).");
            return;
        }
        if (maxMarks <= 0) {
            showError("Maximum Marks must be a positive number greater than zero.");
            return;
        }
        if (maxMarks > 1000) {
            showError("Maximum Marks cannot exceed 1000.");
            return;
        }

        String formattedDate = date.format(DISPLAY_FORMAT);

        // ── Persist to local list ─────────────────────────────────────────────
        if (editingAssessment != null) {
            // Edit existing
            editingAssessment.setName(name);
            editingAssessment.setClassName(cls);
            editingAssessment.setSubject(subject);
            editingAssessment.setTopic(topic);
            editingAssessment.setDate(formattedDate);
            editingAssessment.setMaxMarks(maxMarks);
        } else {
            // Create new
            assessments.add(new Assessment(name, cls, subject, topic,
                                           formattedDate, maxMarks, "Draft"));
        }

        refreshList();

        // ── Success feedback — close form after brief pause ───────────────────
        showSuccess(editingAssessment != null
                ? "Assessment updated successfully."
                : "Assessment '" + name + "' created as Draft.");

        // Close form after 1.2 seconds so the user sees the toast
        javafx.animation.PauseTransition pause =
                new javafx.animation.PauseTransition(
                        javafx.util.Duration.millis(1200));
        pause.setOnFinished(evt -> showForm(false));
        pause.play();
    }

    // =========================================================================
    // Utility — error / success labels
    // =========================================================================

    private void showError(String msg) {
        errorLabel.setText("⚠  " + msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        successLabel.setVisible(false);
        successLabel.setManaged(false);
    }

    private void showSuccess(String msg) {
        successLabel.setText("✓  " + msg);
        successLabel.setVisible(true);
        successLabel.setManaged(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void hideMessages() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        successLabel.setVisible(false);
        successLabel.setManaged(false);
    }
}
