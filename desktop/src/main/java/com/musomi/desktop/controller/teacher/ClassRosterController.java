package com.musomi.desktop.controller.teacher;

import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Controller for the Class Roster screen (/fxml/teacher/class-roster.fxml).
 *
 * Displays a searchable list of students for a specific class.
 * Uses local mock student data — backend integration can replace
 * the MOCK_STUDENTS list once the endpoint is ready.
 */
public class ClassRosterController {

    // -------------------------------------------------------------------------
    // Inner model
    // -------------------------------------------------------------------------

    public static class StudentRow {
        private final String number;
        private final String admNo;
        private final String name;
        private final String gender;
        private final String status;

        public StudentRow(String number, String admNo, String name,
                          String gender, String status) {
            this.number = number;
            this.admNo  = admNo;
            this.name   = name;
            this.gender = gender;
            this.status = status;
        }

        public String getNumber() { return number; }
        public String getAdmNo()  { return admNo;  }
        public String getName()   { return name;   }
        public String getGender() { return gender; }
        public String getStatus() { return status; }
    }

    // -------------------------------------------------------------------------
    // Mock students — replace with real API data later
    // -------------------------------------------------------------------------

    private static final List<StudentRow> MOCK_STUDENTS = List.of(
        new StudentRow("1",  "ADM-2024-001", "John Mugisha",      "Male",   "Active"),
        new StudentRow("2",  "ADM-2024-002", "Sarah Namukasa",    "Female", "Active"),
        new StudentRow("3",  "ADM-2024-003", "Brian Okello",      "Male",   "Active"),
        new StudentRow("4",  "ADM-2024-004", "Mary Achieng",      "Female", "Active"),
        new StudentRow("5",  "ADM-2024-005", "David Kato",        "Male",   "Active"),
        new StudentRow("6",  "ADM-2024-006", "Grace Nakato",      "Female", "Active"),
        new StudentRow("7",  "ADM-2024-007", "Daniel Ouma",       "Male",   "Active"),
        new StudentRow("8",  "ADM-2024-008", "Rebecca Atim",      "Female", "Active"),
        new StudentRow("9",  "ADM-2024-009", "Peter Ssekandi",    "Male",   "Active"),
        new StudentRow("10", "ADM-2024-010", "Esther Nankya",     "Female", "Active"),
        new StudentRow("11", "ADM-2024-011", "Samuel Tumusiime",  "Male",   "Inactive"),
        new StudentRow("12", "ADM-2024-012", "Angela Nabirye",    "Female", "Active")
    );

    // -------------------------------------------------------------------------
    // FXML bindings
    // -------------------------------------------------------------------------

    @FXML private Label                            classNameLabel;
    @FXML private Label                            studentCountLabel;
    @FXML private TextField                        searchField;
    @FXML private TableView<StudentRow>            rosterTable;
    @FXML private TableColumn<StudentRow, String>  numberColumn;
    @FXML private TableColumn<StudentRow, String>  admNoColumn;
    @FXML private TableColumn<StudentRow, String>  nameColumn;
    @FXML private TableColumn<StudentRow, String>  genderColumn;
    @FXML private TableColumn<StudentRow, String>  statusColumn;
    @FXML private VBox                             emptyState;

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private final ObservableList<StudentRow> allStudents =
            FXCollections.observableArrayList();

    private FilteredList<StudentRow> filteredStudents;

    /** Called by MyClassesController after FXML load to inject context. */
    private Runnable backCallback;

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @FXML
    private void initialize() {
        setupTableColumns();

        filteredStudents = new FilteredList<>(allStudents, row -> true);
        rosterTable.setItems(filteredStudents);

        rosterTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        setupSearch();
    }

    // -------------------------------------------------------------------------
    // Public API — called by MyClassesController
    // -------------------------------------------------------------------------

    /**
     * Injects the selected class context into this controller.
     *
     * @param className   display name of the class (e.g. "S3 Blue")
     * @param studentCount enrolled student count shown in subtitle
     * @param backCallback runnable to invoke when the Back button is clicked
     */
    public void setClassData(String className, int studentCount,
                             Runnable backCallback) {
        this.backCallback = backCallback;

        if (classNameLabel != null) {
            classNameLabel.setText(className);
        }
        if (studentCountLabel != null) {
            studentCountLabel.setText(studentCount + " students");
        }

        // Load mock students (same list for every class for now)
        allStudents.setAll(MOCK_STUDENTS);
        updateEmptyState();
    }

    // -------------------------------------------------------------------------
    // Event handlers
    // -------------------------------------------------------------------------

    @FXML
    private void handleBack() {
        if (backCallback != null) {
            backCallback.run();
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void setupTableColumns() {
        numberColumn.setCellValueFactory(
                d -> new SimpleStringProperty(d.getValue().getNumber())
        );
        admNoColumn.setCellValueFactory(
                d -> new SimpleStringProperty(d.getValue().getAdmNo())
        );
        nameColumn.setCellValueFactory(
                d -> new SimpleStringProperty(d.getValue().getName())
        );
        genderColumn.setCellValueFactory(
                d -> new SimpleStringProperty(d.getValue().getGender())
        );
        statusColumn.setCellValueFactory(
                d -> new SimpleStringProperty(d.getValue().getStatus())
        );
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String term = newVal == null ? "" : newVal.trim().toLowerCase();

            filteredStudents.setPredicate(row -> {
                if (term.isEmpty()) return true;
                return row.getName().toLowerCase().contains(term)
                    || row.getAdmNo().toLowerCase().contains(term);
            });

            updateEmptyState();
        });
    }

    private void updateEmptyState() {
        boolean isEmpty = filteredStudents.isEmpty();
        emptyState.setVisible(isEmpty);
        emptyState.setManaged(isEmpty);
        rosterTable.setVisible(!isEmpty);
        rosterTable.setManaged(!isEmpty);
    }
}
