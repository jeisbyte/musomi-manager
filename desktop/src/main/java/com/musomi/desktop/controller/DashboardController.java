package com.musomi.desktop.controller;

import java.util.List;
import java.util.function.Consumer;

import com.musomi.desktop.controller.teacher.AssessmentStore;
import com.musomi.desktop.controller.teacher.AssessmentStore.Assessment;
import com.musomi.desktop.config.Session;
import com.musomi.desktop.model.dto.UserResponse;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML private Label myClassesCount;
    @FXML private Label totalStudentsCount;
    @FXML private Label pendingMarksCount;
    @FXML private Label publishedAssessmentsCount;
    @FXML private Label pendingReminderText;
    @FXML private Label greetingLabel;
    @FXML private Button markEntryButton;

    private Consumer<Assessment> onOpenMarkEntry;

    @FXML
    private void initialize() {
        refreshPendingReminder();
        refreshSummaryCards();
    }

    public void setOnOpenMarkEntry(Consumer<Assessment> onOpenMarkEntry) {
        this.onOpenMarkEntry = onOpenMarkEntry;
    }

    @FXML
    private void goToMarkEntry() {
        List<Assessment> pending = AssessmentStore.getPendingAssessments();
        if (!pending.isEmpty() && onOpenMarkEntry != null) {
            onOpenMarkEntry.accept(pending.get(0));
        }
    }

    private void refreshPendingReminder() {
        int count = AssessmentStore.getPendingAssessments().size();
        pendingMarksCount.setText(String.valueOf(count));
        pendingReminderText.setText(count == 0
                ? "All assessments are fully marked and published."
                : count + (count == 1
                        ? " assessment is incomplete or not yet published."
                        : " assessments are incomplete or not yet published."));
        markEntryButton.setVisible(count > 0);
        markEntryButton.setManaged(count > 0);
    }

    private void refreshSummaryCards() {
        UserResponse user = Session.getInstance().getUser();
        if (user == null || user.getFullName() == null || user.getFullName().isBlank()) {
            greetingLabel.setText("Welcome back");
        } else {
            String firstName = user.getFullName().trim().split("\\s+")[0];
            greetingLabel.setText("Good morning, " + firstName);
        }

        myClassesCount.setText(String.valueOf(AssessmentStore.countDistinctClasses()));
        totalStudentsCount.setText(String.valueOf(AssessmentStore.mockRosterSize()));
        publishedAssessmentsCount.setText(String.valueOf(AssessmentStore.countPublished()));
    }
}
