package com.musomi.desktop.controller.teacher;

import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Controller for the My Classes screen (/fxml/teacher/my-classes.fxml).
 *
 * Uses local mock data — does NOT call the backend.
 * Mock data can be swapped for TeacherClassService.getMyClasses() once the
 * backend endpoint /teacher/classes is ready.
 */
public class MyClassesController {

    // -------------------------------------------------------------------------
    // Inner model — mirrors TeacherClassResponse but is fully local
    // -------------------------------------------------------------------------

    public static class ClassCard {
        private final String  className;
        private final String  subject;
        private final int     students;
        private final int     assessments;
        private final int     pendingMarks;

        public ClassCard(String className, String subject,
                         int students, int assessments, int pendingMarks) {
            this.className    = className;
            this.subject      = subject;
            this.students     = students;
            this.assessments  = assessments;
            this.pendingMarks = pendingMarks;
        }

        public String getClassName()   { return className;    }
        public String getSubject()     { return subject;      }
        public int    getStudents()    { return students;     }
        public int    getAssessments() { return assessments;  }
        public int    getPendingMarks(){ return pendingMarks; }
    }

    // -------------------------------------------------------------------------
    // Mock data — isolated for easy backend swap-out later
    // -------------------------------------------------------------------------

    private static final List<ClassCard> MOCK_CLASSES = List.of(
        new ClassCard("S3 Blue", "Mathematics", 42, 8, 2),
        new ClassCard("S3 Red",  "Mathematics", 39, 7, 1),
        new ClassCard("S4 Blue", "Mathematics", 41, 9, 3),
        new ClassCard("S4 Red",  "Mathematics", 38, 6, 0),
        new ClassCard("S5",      "Mathematics", 20, 5, 1)
    );

    // -------------------------------------------------------------------------
    // FXML bindings
    // -------------------------------------------------------------------------

    @FXML
    private FlowPane cardsPane;

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @FXML
    private void initialize() {
        buildCards(MOCK_CLASSES);
    }

    // -------------------------------------------------------------------------
    // Card building
    // -------------------------------------------------------------------------

    private void buildCards(List<ClassCard> classes) {
        cardsPane.getChildren().clear();

        for (ClassCard cls : classes) {
            cardsPane.getChildren().add(buildCard(cls));
        }
    }

    /** Builds a single class card matching the Musomi design system. */
    private VBox buildCard(ClassCard cls) {

        // ── Card container ──────────────────────────────────────────────────
        VBox card = new VBox(14);
        card.setPrefWidth(280);
        card.setMaxWidth(320);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: #FFFFFF;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: #E2E8F0;" +
            "-fx-border-radius: 12px;" +
            "-fx-border-width: 1px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);"
        );

        // ── Class name ───────────────────────────────────────────────────────
        Label nameLabel = new Label(cls.getClassName());
        nameLabel.setStyle(
            "-fx-font-size: 20px;" +
            "-fx-font-weight: 700;" +
            "-fx-text-fill: #0F172A;"
        );

        // ── Subject ──────────────────────────────────────────────────────────
        Label subjectLabel = new Label(cls.getSubject());
        subjectLabel.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #64748B;"
        );

        // ── Separator region ─────────────────────────────────────────────────
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: #F1F5F9;");

        // ── Stats row ────────────────────────────────────────────────────────
        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.getChildren().addAll(
            buildStat(String.valueOf(cls.getStudents()), "Students"),
            buildStat(String.valueOf(cls.getAssessments()), "Assessments")
        );

        // ── Pending marks badge ───────────────────────────────────────────────
        HBox pendingRow = new HBox(8);
        pendingRow.setAlignment(Pos.CENTER_LEFT);

        int pending = cls.getPendingMarks();
        Label pendingLabel;

        if (pending > 0) {
            pendingLabel = new Label(pending + " Pending Mark" + (pending == 1 ? "" : "s"));
            pendingLabel.setStyle(
                "-fx-background-color: #FFFBEB;" +
                "-fx-text-fill: #B45309;" +
                "-fx-padding: 4 10;" +
                "-fx-background-radius: 999px;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 600;"
            );
        } else {
            pendingLabel = new Label("All Marks Up-to-Date");
            pendingLabel.setStyle(
                "-fx-background-color: #ECFDF5;" +
                "-fx-text-fill: #047857;" +
                "-fx-padding: 4 10;" +
                "-fx-background-radius: 999px;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 600;"
            );
        }
        pendingRow.getChildren().add(pendingLabel);

        // ── View Class button ─────────────────────────────────────────────────
        Button viewBtn = new Button("View Class →");
        viewBtn.setMaxWidth(Double.MAX_VALUE);
        viewBtn.setStyle(
            "-fx-background-color: #4F46E5;" +
            "-fx-text-fill: #FFFFFF;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10 18;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 14px;" +
            "-fx-cursor: hand;"
        );
        viewBtn.setOnAction(e -> openRoster(cls));
        VBox.setVgrow(viewBtn, Priority.NEVER);

        card.getChildren().addAll(nameLabel, subjectLabel, sep, statsRow, pendingRow, viewBtn);
        return card;
    }

    /** Builds a small vertical stat: big number + label below it. */
    private VBox buildStat(String value, String label) {
        VBox stat = new VBox(2);
        stat.setAlignment(Pos.CENTER_LEFT);

        Label valLabel = new Label(value);
        valLabel.setStyle(
            "-fx-font-size: 22px;" +
            "-fx-font-weight: 700;" +
            "-fx-text-fill: #0F172A;"
        );

        Label txtLabel = new Label(label);
        txtLabel.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #64748B;"
        );

        stat.getChildren().addAll(valLabel, txtLabel);
        return stat;
    }

    // -------------------------------------------------------------------------
    // Navigation to Class Roster
    // -------------------------------------------------------------------------

    private javafx.scene.layout.StackPane findContentArea() {
        javafx.scene.Node current = cardsPane;
        while (current != null) {
            if ("contentArea".equals(current.getId()) && current instanceof javafx.scene.layout.StackPane sp) {
                return sp;
            }
            current = current.getParent();
        }
        if (cardsPane != null && cardsPane.getScene() != null) {
            javafx.scene.Node node = cardsPane.getScene().lookup("#contentArea");
            if (node instanceof javafx.scene.layout.StackPane sp) {
                return sp;
            }
        }
        return null;
    }

    private void openRoster(ClassCard cls) {
        try {
            javafx.scene.layout.StackPane sp = findContentArea();
            if (sp == null) {
                System.err.println("Could not locate contentArea StackPane");
                return;
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/teacher/class-roster.fxml")
            );
            Node roster = loader.load();

            ClassRosterController rosterCtrl = loader.getController();
            rosterCtrl.setClassData(
                    cls.getClassName(),
                    cls.getStudents(),
                    () -> returnToMyClasses(sp)
            );

            // Replace the content in the parent StackPane
            sp.getChildren().setAll(roster);

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("FAILED TO LOAD class-roster.fxml: " + ex.getMessage());
        }
    }

    /** Called by ClassRosterController when the Back button is pressed. */
    private void returnToMyClasses(javafx.scene.layout.StackPane sp) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/teacher/my-classes.fxml")
            );
            Node myClasses = loader.load();
            if (sp != null) {
                sp.getChildren().setAll(myClasses);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
