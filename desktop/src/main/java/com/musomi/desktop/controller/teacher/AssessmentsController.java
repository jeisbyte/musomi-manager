package com.musomi.desktop.controller.teacher;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.musomi.desktop.controller.teacher.AssessmentStore.Assessment;
import com.musomi.desktop.controller.teacher.AssessmentStore.StudentMark;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Controller for /fxml/teacher/assessments.fxml.
 *
 * Data is sourced from {@link AssessmentStore} — a shared, in-process store.
 * Replace store calls with API calls once the backend is ready.
 *
 * Panels (all live inside the same FXML StackPane):
 *   listPane   — assessment list table
 *   formPane   — create / edit form overlay
 *   reviewPane — review & publish overlay
 *   viewPane   — read-only view for published assessments
 */
public class AssessmentsController {

    // =========================================================================
    // Combo options
    // =========================================================================

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
    // FXML — list panel
    // =========================================================================

    @FXML private Button  createBtn;
    @FXML private VBox    listPane;
    @FXML private VBox    rowsContainer;
    @FXML private VBox    emptyState;

    // =========================================================================
    // FXML — create / edit form panel
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

    // =========================================================================
    // FXML — review / publish panel
    // =========================================================================

    @FXML private ScrollPane reviewPane;
    @FXML private Label  reviewNameLabel;
    @FXML private Label  reviewClassLabel;
    @FXML private Label  reviewSubjectLabel;
    @FXML private Label  reviewTopicLabel;
    @FXML private Label  reviewDateLabel;
    @FXML private Label  reviewMaxMarksLabel;
    @FXML private Label  reviewCompletenessLabel;
    @FXML private Label  reviewWarningLabel;
    @FXML private VBox   reviewMarksContainer;
    @FXML private Button publishBtn;

    // =========================================================================
    // FXML — view (read-only) panel
    // =========================================================================

    @FXML private ScrollPane viewPane;
    @FXML private Label  viewNameLabel;
    @FXML private Label  viewClassLabel;
    @FXML private Label  viewSubjectLabel;
    @FXML private Label  viewTopicLabel;
    @FXML private Label  viewDateLabel;
    @FXML private Label  viewMaxMarksLabel;
    @FXML private VBox   viewMarksContainer;

    // =========================================================================
    // State
    // =========================================================================

    /** Assessment currently being edited in the form. */
    private Assessment editingAssessment = null;

    /** Assessment currently open in the review pane. */
    private Assessment reviewingAssessment = null;

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

        List<Assessment> assessments = AssessmentStore.getAssessments();
        boolean hasRows = !assessments.isEmpty();
        emptyState.setVisible(!hasRows);
        emptyState.setManaged(!hasRows);

        for (int i = 0; i < assessments.size(); i++) {
            Assessment a = assessments.get(i);
            boolean isEven = (i % 2 == 0);
            rowsContainer.getChildren().add(buildRow(a, isEven));
            if (i < assessments.size() - 1) {
                Separator sep = new Separator();
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
        Label nameLabel = new Label(a.getName());
        nameLabel.setMinWidth(200);
        nameLabel.setPrefWidth(200);
        nameLabel.setStyle("-fx-font-size:13px;-fx-font-weight:600;-fx-text-fill:#0F172A;");

        // Class
        Label clsLabel   = cell(a.getClassName(), 100);
        Label subLabel   = cell(a.getSubject(),   130);
        Label topLabel   = cell(a.getTopic(),     150);
        Label dateLabel  = cell(a.getDate(),      110);
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

        // Action buttons
        HBox actionCell = new HBox(6);
        actionCell.setMinWidth(180);
        actionCell.setAlignment(Pos.CENTER_LEFT);

        if ("Draft".equals(a.getStatus())) {
            Button editBtn = actionButton("Edit", "#EEF2FF", "#4F46E5");
            editBtn.setOnAction(e -> openEditForm(a));

            Button reviewPublishBtn = actionButton("Review & Publish", "#FFF7ED", "#B45309");
            reviewPublishBtn.setOnAction(e -> openReviewPane(a));

            actionCell.getChildren().addAll(editBtn, reviewPublishBtn);
        } else {
            Button viewBtn = actionButton("View", "#F0FDF4", "#047857");
            viewBtn.setOnAction(e -> openViewPane(a));
            actionCell.getChildren().add(viewBtn);
        }

        row.getChildren().addAll(
                nameLabel, clsLabel, subLabel, topLabel,
                dateLabel, marksLabel, statusCell, actionCell);
        return row;
    }

    private Button actionButton(String text, String bgColor, String textColor) {
        Button b = new Button(text);
        b.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: 600;" +
            "-fx-background-radius: 6px;" +
            "-fx-padding: 5 10;" +
            "-fx-cursor: hand;");
        return b;
    }

    private Label cell(String text, double minWidth) {
        Label lbl = new Label(text);
        lbl.setMinWidth(minWidth);
        lbl.setPrefWidth(minWidth);
        lbl.setStyle("-fx-font-size:13px;-fx-text-fill:#475569;");
        return lbl;
    }

    // =========================================================================
    // Panel visibility
    // =========================================================================

    private enum ActivePanel { LIST, FORM, REVIEW, VIEW }

    private void showPanel(ActivePanel panel) {
        boolean showList   = panel == ActivePanel.LIST;
        boolean showForm   = panel == ActivePanel.FORM;
        boolean showReview = panel == ActivePanel.REVIEW;
        boolean showView   = panel == ActivePanel.VIEW;

        listPane.setVisible(showList);
        listPane.setManaged(showList);
        createBtn.setDisable(!showList);

        formPane.setVisible(showForm);
        formPane.setManaged(showForm);

        reviewPane.setVisible(showReview);
        reviewPane.setManaged(showReview);

        viewPane.setVisible(showView);
        viewPane.setManaged(showView);

        if (showList) hideMessages();
    }

    // =========================================================================
    // Create / Edit form
    // =========================================================================

    @FXML
    private void handleCreateAssessment() {
        editingAssessment = null;
        formTitle.setText("Create Assessment");
        saveDraftBtn.setText("Save Draft");
        clearForm();
        showPanel(ActivePanel.FORM);
    }

    private void openEditForm(Assessment a) {
        editingAssessment = a;
        formTitle.setText("Edit Assessment");
        saveDraftBtn.setText("Save Changes");
        populateForm(a);
        showPanel(ActivePanel.FORM);
    }

    @FXML
    private void handleCancelForm() {
        showPanel(ActivePanel.LIST);
    }

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
        datePicker.setValue(null); // stored as string; leave for re-selection
        hideMessages();
    }

    @FXML
    private void handleSaveDraft() {
        hideMessages();

        String name     = nameField.getText()    == null ? "" : nameField.getText().trim();
        String cls      = classCombo.getValue()  == null ? "" : classCombo.getValue().trim();
        String subject  = subjectCombo.getValue()== null ? "" : subjectCombo.getValue().trim();
        String topic    = topicCombo.getValue()  == null ? "" : topicCombo.getValue().trim();
        LocalDate date  = datePicker.getValue();
        String marksStr = marksField.getText()   == null ? "" : marksField.getText().trim();

        if (name.isEmpty())    { showError("Assessment Name is required."); return; }
        if (cls.isEmpty())     { showError("Class is required."); return; }
        if (subject.isEmpty()) { showError("Subject is required."); return; }
        if (topic.isEmpty())   { showError("Topic is required."); return; }
        if (date == null)      { showError("Assessment Date is required."); return; }
        if (marksStr.isEmpty()){ showError("Maximum Marks is required."); return; }

        int maxMarks;
        try {
            maxMarks = Integer.parseInt(marksStr);
        } catch (NumberFormatException ex) {
            showError("Maximum Marks must be a whole number (e.g. 40).");
            return;
        }
        if (maxMarks <= 0)    { showError("Maximum Marks must be a positive number greater than zero."); return; }
        if (maxMarks > 1000)  { showError("Maximum Marks cannot exceed 1000."); return; }

        String formattedDate = date.format(DISPLAY_FORMAT);

        if (editingAssessment != null) {
            editingAssessment.setName(name);
            editingAssessment.setClassName(cls);
            editingAssessment.setSubject(subject);
            editingAssessment.setTopic(topic);
            editingAssessment.setDate(formattedDate);
            editingAssessment.setMaxMarks(maxMarks);
        } else {
            AssessmentStore.addAssessment(name, cls, subject, topic, formattedDate, maxMarks);
        }

        refreshList();

        showSuccess(editingAssessment != null
                ? "Assessment updated successfully."
                : "Assessment '" + name + "' created as Draft.");

        javafx.animation.PauseTransition pause =
                new javafx.animation.PauseTransition(javafx.util.Duration.millis(1200));
        pause.setOnFinished(evt -> showPanel(ActivePanel.LIST));
        pause.play();
    }

    // =========================================================================
    // Review & Publish panel
    // =========================================================================

    private void openReviewPane(Assessment a) {
        reviewingAssessment = a;

        // Populate header details
        reviewNameLabel.setText(a.getName());
        reviewClassLabel.setText(a.getClassName());
        reviewSubjectLabel.setText(a.getSubject());
        reviewTopicLabel.setText(a.getTopic());
        reviewDateLabel.setText(a.getDate());
        reviewMaxMarksLabel.setText(String.valueOf(a.getMaxMarks()));

        // Populate marks table
        List<StudentMark> marks = AssessmentStore.getMarks(a);
        int completed = AssessmentStore.countCompleted(a);
        int total = marks.size();

        reviewCompletenessLabel.setText(completed + " of " + total + " students have marks entered.");

        boolean allComplete = AssessmentStore.allMarksComplete(a);
        if (!allComplete) {
            int missing = total - completed;
            reviewWarningLabel.setText(
                "⚠  " + missing + " student" + (missing == 1 ? "" : "s") +
                " still " + (missing == 1 ? "does" : "do") + " not have a valid mark entered. " +
                "All marks must be complete before publishing.");
            reviewWarningLabel.setVisible(true);
            reviewWarningLabel.setManaged(true);
            publishBtn.setDisable(true);
            publishBtn.setStyle(
                "-fx-background-color: #CBD5E1;" +
                "-fx-text-fill: #94A3B8;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: 700;" +
                "-fx-background-radius: 8px;" +
                "-fx-padding: 10 26;" +
                "-fx-cursor: default;");
        } else {
            reviewWarningLabel.setVisible(false);
            reviewWarningLabel.setManaged(false);
            publishBtn.setDisable(false);
            publishBtn.setStyle(
                "-fx-background-color: #059669;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: 700;" +
                "-fx-background-radius: 8px;" +
                "-fx-padding: 10 26;" +
                "-fx-cursor: hand;");
        }

        buildMarksRows(reviewMarksContainer, marks, a.getMaxMarks(), false);

        showPanel(ActivePanel.REVIEW);
    }

    @FXML
    private void handleCancelReview() {
        reviewingAssessment = null;
        showPanel(ActivePanel.LIST);
    }

    @FXML
    private void handlePublish() {
        if (reviewingAssessment == null) return;
        if (!AssessmentStore.allMarksComplete(reviewingAssessment)) return;

        Assessment a = reviewingAssessment;
        List<StudentMark> marks = AssessmentStore.getMarks(a);
        int total = marks.size();

        // ── Confirmation dialog ───────────────────────────────────────────────
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Publish Assessment");
        confirm.setHeaderText("Publish: " + a.getName());
        confirm.setContentText(
            "Assessment:    " + a.getName() + "\n" +
            "Class:             " + a.getClassName() + "\n" +
            "Students:       " + total + "\n" +
            "Completed:     " + total + " / " + total + "\n" +
            "Max Mark:      " + a.getMaxMarks() + "\n\n" +
            "Published assessments become read-only and cannot be edited.\n" +
            "Are you sure you want to publish?");

        confirm.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
        // Relabel OK button
        ((javafx.scene.control.ButtonBar) confirm.getDialogPane().lookup(".button-bar"))
                .getButtons()
                .stream()
                .filter(n -> n instanceof Button &&
                        ButtonType.OK.getText().equals(((Button) n).getText()))
                .findFirst()
                .ifPresent(n -> ((Button) n).setText("Publish"));

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        // ── Perform publish ───────────────────────────────────────────────────
        AssessmentStore.publish(a);
        reviewingAssessment = null;

        refreshList();
        showPanel(ActivePanel.LIST);

        // Show brief success toast on the list
        showListSuccess("Assessment [" + a.getName() + "] published successfully.");
    }

    // =========================================================================
    // View (read-only) panel
    // =========================================================================

    private void openViewPane(Assessment a) {
        viewNameLabel.setText(a.getName());
        viewClassLabel.setText(a.getClassName());
        viewSubjectLabel.setText(a.getSubject());
        viewTopicLabel.setText(a.getTopic());
        viewDateLabel.setText(a.getDate());
        viewMaxMarksLabel.setText(String.valueOf(a.getMaxMarks()));

        List<StudentMark> marks = AssessmentStore.getMarks(a);
        buildMarksRows(viewMarksContainer, marks, a.getMaxMarks(), false);

        showPanel(ActivePanel.VIEW);
    }

    @FXML
    private void handleCloseView() {
        showPanel(ActivePanel.LIST);
    }

    // =========================================================================
    // Marks table builder (shared by review and view panels)
    // =========================================================================

    private void buildMarksRows(VBox container, List<StudentMark> marks,
                                int maxMarks, boolean editable) {
        container.getChildren().clear();

        // Header row
        HBox header = new HBox(0);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 16, 10, 16));
        header.setStyle(
            "-fx-background-color: #F8FAFC;" +
            "-fx-border-color: #E2E8F0;" +
            "-fx-border-width: 1 1 0 1;" +
            "-fx-background-radius: 8px 8px 0 0;");
        header.getChildren().addAll(
            colHeader("#", 40),
            colHeader("Student", 200),
            colHeader("Mark", 80),
            colHeader("Max", 60),
            colHeader("Grade", 70),
            colHeader("Remark", 120)
        );
        container.getChildren().add(header);

        for (int i = 0; i < marks.size(); i++) {
            StudentMark sm = marks.get(i);
            boolean isEven = (i % 2 == 0);
            String bg = isEven ? "white" : "#FAFBFC";

            HBox row = new HBox(0);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(11, 16, 11, 16));
            row.setStyle(
                "-fx-background-color: " + bg + ";" +
                "-fx-border-color: #E2E8F0;" +
                "-fx-border-width: 0 1 1 1;");

            boolean valid = sm.hasValidMark(maxMarks);
            String markText = valid ? sm.getMark() : "—";
            String gradeText = sm.computeGrade(maxMarks);
            String remarkText = sm.computeRemark(maxMarks);

            Label markLabel = new Label(markText);
            markLabel.setMinWidth(80);
            markLabel.setStyle(
                "-fx-font-size:13px;" +
                (valid ? "-fx-text-fill:#0F172A;-fx-font-weight:600;"
                       : "-fx-text-fill:#EF4444;-fx-font-weight:500;"));

            Label gradeLabel = new Label(gradeText);
            gradeLabel.setMinWidth(70);
            gradeLabel.setStyle(
                "-fx-font-size:13px;" +
                "-fx-font-weight:600;" +
                gradeColor(gradeText));

            row.getChildren().addAll(
                colValue(sm.getNumber(), 40),
                colValue(sm.getStudent(), 200),
                markLabel,
                colValue(String.valueOf(maxMarks), 60),
                gradeLabel,
                colValue(remarkText, 120)
            );
            container.getChildren().add(row);
        }
    }

    private Label colHeader(String text, double width) {
        Label l = new Label(text);
        l.setMinWidth(width);
        l.setPrefWidth(width);
        l.setStyle("-fx-font-size:12px;-fx-font-weight:700;-fx-text-fill:#64748B;");
        return l;
    }

    private Label colValue(String text, double width) {
        Label l = new Label(text);
        l.setMinWidth(width);
        l.setPrefWidth(width);
        l.setStyle("-fx-font-size:13px;-fx-text-fill:#475569;");
        return l;
    }

    private String gradeColor(String grade) {
        return switch (grade) {
            case "A"  -> "-fx-text-fill:#047857;";
            case "B"  -> "-fx-text-fill:#0369A1;";
            case "C"  -> "-fx-text-fill:#6D28D9;";
            case "D"  -> "-fx-text-fill:#B45309;";
            case "E","F" -> "-fx-text-fill:#DC2626;";
            default   -> "-fx-text-fill:#94A3B8;";
        };
    }

    // =========================================================================
    // Messages
    // =========================================================================

    @FXML private Label listSuccessLabel;

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

    private void showListSuccess(String msg) {
        listSuccessLabel.setText("✓  " + msg);
        listSuccessLabel.setVisible(true);
        listSuccessLabel.setManaged(true);
        javafx.animation.PauseTransition t =
                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(4));
        t.setOnFinished(e -> {
            listSuccessLabel.setVisible(false);
            listSuccessLabel.setManaged(false);
        });
        t.play();
    }

    private void hideMessages() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        successLabel.setVisible(false);
        successLabel.setManaged(false);
    }
}
