# DESKTOP.md — JavaFX Developer Specialist Document


## 1. Who This Is For

You are the **JavaFX Developer** on Musomi Manager.

You build the desktop app that teachers and admins use every day on school PCs.

You work alongside:
- **Backend Lead** — provides the REST API you call
- **Web Developer** — shares the design system with you
- **Database + DevOps** — owns the schema your models mirror
- **Product + QA** — tests your screens

You own the desktop app. You don't edit backend code or web templates.

---

## 2. What You Build in v1

- JavaFX desktop application for Windows
- Login screen with session handling
- Admin screens (users, classes, subjects, topics, students, settings, audit log)
- Teacher screens (dashboard, class roster, assessment create, **mark entry grid**, publish, comments)
- Report generation and preview
- FXML layouts styled with JavaFX CSS
- API client to talk to the backend
- Keyboard-driven mark entry (Tab, Enter, arrows, Escape)
- Auto-save on cell commit
- Background tasks (no blocking UI)
- Toast notifications and modals
- Packaging as `.msi` installer (jpackage)

**Not in v1:** offline mode, notifications, sharing, AI, analytics. Those are v2–v6.

---

## 3. Package Structure

```
desktop/src/main/java/com/musomi/desktop/
│
├── DesktopApplication.java
│
├── config/
│   ├── AppConfig.java                ← backend URL, settings
│   ├── Session.java                  ← current user, token
│   ├── SceneManager.java             ← navigation between screens
│   └── ApiConfig.java
│
├── api/
│   ├── ApiClient.java                ← HTTP wrapper
│   ├── AuthApi.java
│   ├── AdminUserApi.java
│   ├── AdminSchoolApi.java
│   ├── AdminStudentApi.java
│   ├── TeacherAssessmentApi.java
│   ├── TeacherMarkApi.java
│   ├── TeacherCommentApi.java
│   └── ReportApi.java
│
├── model/
│   ├── User.java
│   ├── Student.java
│   ├── AcademicYear.java
│   ├── Term.java
│   ├── ClassLevel.java
│   ├── ClassModel.java
│   ├── Stream.java
│   ├── Subject.java
│   ├── Topic.java
│   ├── Assessment.java
│   ├── Score.java
│   └── Comment.java
│
├── controller/
│   ├── LoginController.java
│   ├── MainLayoutController.java
│   ├── DashboardController.java
│   ├── admin/
│   │   ├── UserListController.java
│   │   ├── UserFormController.java
│   │   ├── ClassListController.java
│   │   ├── ClassFormController.java
│   │   ├── SubjectListController.java
│   │   ├── SubjectFormController.java
│   │   ├── TopicListController.java
│   │   ├── TopicFormController.java
│   │   ├── StudentListController.java
│   │   ├── StudentFormController.java
│   │   ├── ImportStudentsController.java
│   │   ├── TeacherAssignmentController.java
│   │   ├── SchoolSettingsController.java
│   │   └── AuditLogController.java
│   ├── teacher/
│   │   ├── MyClassesController.java
│   │   ├── ClassRosterController.java
│   │   ├── AssessmentCreateController.java
│   │   ├── MarkEntryController.java        ← the critical one
│   │   ├── PublishController.java
│   │   └── CommentController.java
│   └── report/
│       ├── GenerateReportController.java
│       └── ReportPreviewController.java
│
├── service/
│   ├── AuthService.java
│   ├── UserService.java
│   ├── StudentService.java
│   ├── AssessmentService.java
│   ├── MarkService.java
│   └── ReportService.java
│
├── util/
│   ├── Toast.java                    ← toast notifications
│   ├── Modal.java                    ← dialog helpers
│   ├── FormValidator.java            ← input validation
│   ├── DateFormatter.java
│   ├── GradeFormatter.java
│   └── TaskRunner.java               ← background tasks
│
└── pdf/
    └── ReportViewer.java
```

Resources:

```
desktop/src/main/resources/
├── fxml/
│   ├── login.fxml
│   ├── main-layout.fxml
│   ├── dashboard.fxml
│   ├── admin/
│   ├── teacher/
│   └── report/
├── css/
│   ├── styles.css
│   ├── components.css
│   └── themes/light.css
├── icons/
│   ├── logo.png
│   └── app-icon.ico
├── config.properties
└── logback.xml
```

---

## 4. Architecture

Layered, same as backend:

```
┌────────────────────────────────────────┐
│  PRESENTATION (FXML + CSS)             │
│  - login.fxml, mark-entry.fxml, etc.   │
└─────────────────┬──────────────────────┘
                  │
┌─────────────────▼──────────────────────┐
│  CONTROLLER (@FXML)                    │
│  - LoginController, MarkEntryController│
│  - Handle events, call services        │
└─────────────────┬──────────────────────┘
                  │
┌─────────────────▼──────────────────────┐
│  SERVICE (client-side)                 │
│  - AuthService, MarkService            │
│  - Call ApiClient, manage state        │
└─────────────────┬──────────────────────┘
                  │
┌─────────────────▼──────────────────────┐
│  API CLIENT (HTTP)                     │
│  - Attaches JWT, parses JSON           │
│  - Returns models or throws exceptions │
└─────────────────┬──────────────────────┘
                  │
                  │ HTTP/JSON over LAN
                  ▼
              Backend API
```

**Rules:**
- FXML only describes layout — no logic
- Controllers handle events and call services
- Services call API client — controllers never call API client directly
- API client handles HTTP, JSON, auth
- No business logic in controllers

---

## 5. Code Conventions

### FXML files

- kebab-case names: `mark-entry.fxml`
- `fx:id` uses camelCase matching the controller field: `fx:id="studentTable"` → `@FXML TableView<Student> studentTable;`
- One root container per FXML
- Use `VBox`, `HBox`, `BorderPane`, `GridPane` for layout
- Use `TableView` for tabular data
- Use `MaterialFX` controls where available (MFXButton, MFXTextField)
- No inline styles — use CSS classes

Example skeleton:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.layout.*?>
<?import javafx.scene.control.*?>
<?import io.github.palexdev.materialfx.controls.*?>

<BorderPane xmlns="http://javafx.com/javafx"
            xmlns:fx="http://javafx.com/fxml"
            fx:controller="com.musomi.desktop.controller.teacher.MarkEntryController"
            styleClass="mark-entry">

    <top>
        <HBox styleClass="page-header" spacing="16">
            <Label fx:id="titleLabel" styleClass="h1"/>
            <Label fx:id="statusBadge" styleClass="badge-draft"/>
        </HBox>
    </top>

    <center>
        <TableView fx:id="studentTable" editable="true">
            <columns>
                <TableColumn fx:id="studentCol" text="Student"/>
                <TableColumn fx:id="scoreCol" text="Score"/>
                <TableColumn fx:id="gradeCol" text="Grade"/>
                <TableColumn fx:id="feedbackCol" text="Feedback"/>
            </columns>
        </TableView>
    </center>

    <bottom>
        <HBox styleClass="page-footer" spacing="12">
            <Label fx:id="progressLabel"/>
            <Region HBox.hgrow="ALWAYS"/>
            <MFXButton fx:id="saveButton" text="Save Draft" styleClass="btn-secondary"/>
            <MFXButton fx:id="publishButton" text="Publish" styleClass="btn-primary"/>
        </HBox>
    </bottom>
</BorderPane>
```

### Controllers

- Annotate fields with `@FXML` — always
- Implement `Initializable` and use `initialize()`
- Use `@FXML private void onSomeAction(ActionEvent event)`
- Never call backend on the JavaFX thread — use `TaskRunner`
- Show errors via `Toast` or `Modal`
- Log at DEBUG for user actions

Example:

```java
@Component
@RequiredArgsConstructor
public class MarkEntryController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private Label statusBadge;
    @FXML private TableView<StudentRow> studentTable;
    @FXML private TableColumn<StudentRow, String> studentCol;
    @FXML private TableColumn<StudentRow, BigDecimal> scoreCol;
    @FXML private TableColumn<StudentRow, String> gradeCol;
    @FXML private TableColumn<StudentRow, String> feedbackCol;
    @FXML private Label progressLabel;
    @FXML private MFXButton saveButton;
    @FXML private MFXButton publishButton;

    private final MarkService markService;
    private final AssessmentService assessmentService;
    private Long assessmentId;
    private ObservableList<StudentRow> rows = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupKeyboard();
        setupButtons();
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
        loadGrid();
    }

    private void setupTable() {
        studentCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("grade"));
        feedbackCol.setCellValueFactory(new PropertyValueFactory<>("feedback"));

        scoreCol.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));
        scoreCol.setOnEditCommit(this::onScoreCommit);

        studentTable.setEditable(true);
        studentTable.setItems(rows);
    }

    private void setupKeyboard() {
        studentTable.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                studentTable.getSelectionModel().selectNext();
                e.consume();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                studentTable.getSelectionModel().clearSelection();
            }
        });
    }

    private void setupButtons() {
        saveButton.setOnAction(e -> saveDraft());
        publishButton.setOnAction(e -> publish());
    }

    private void onScoreCommit(TableColumn.CellEditEvent<StudentRow, BigDecimal> event) {
        StudentRow row = event.getRowValue();
        BigDecimal newScore = event.getNewValue();

        BigDecimal max = assessmentService.getMaxScore(assessmentId);
        if (newScore.compareTo(BigDecimal.ZERO) < 0 || newScore.compareTo(max) > 0) {
            Toast.error("Score must be between 0 and " + max);
            event.getTableView().refresh();
            return;
        }

        row.setScore(newScore);
        row.setGrade(GradeFormatter.from(newScore, max));
        autoSave(row);
    }

    private void loadGrid() {
        TaskRunner.run(
            () -> markService.loadGrid(assessmentId),
            this::populateGrid,
            err -> Toast.error(err.getMessage())
        );
    }

    private void populateGrid(MarkGridResponse data) {
        titleLabel.setText(data.getAssessmentTitle());
        statusBadge.setText(data.getStatus());
        rows.setAll(data.getRows());
        updateProgress();
    }

    private void autoSave(StudentRow row) {
        TaskRunner.run(
            () -> markService.saveScore(row),
            saved -> Toast.success("Saved"),
            err -> Toast.error(err.getMessage())
        );
    }

    private void saveDraft() {
        TaskRunner.run(
            () -> markService.saveAll(assessmentId, rows),
            ok -> Toast.success("Draft saved"),
            err -> Toast.error(err.getMessage())
        );
    }

    private void publish() {
        if (!Modal.confirm("Publish marks? Students will see them immediately.")) return;
        TaskRunner.run(
            () -> markService.publish(assessmentId),
            ok -> { Toast.success("Published"); statusBadge.setText("PUBLISHED"); },
            err -> Toast.error(err.getMessage())
        );
    }

    private void updateProgress() {
        long entered = rows.stream().filter(r -> r.getScore() != null).count();
        progressLabel.setText("Entered: " + entered + "/" + rows.size());
    }
}
```

### Services (client-side)

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class MarkService {

    private final TeacherMarkApi api;

    public MarkGridResponse loadGrid(Long assessmentId) {
        return api.getGrid(assessmentId);
    }

    public ScoreResponse saveScore(StudentRow row) {
        return api.updateScore(row.getScoreId(),
            new SaveMarksRequest(row.getScore(), row.getFeedback()));
    }

    public void saveAll(Long assessmentId, List<StudentRow> rows) {
        api.batchSave(assessmentId, rows);
    }

    public void publish(Long assessmentId) {
        api.publishAssessment(assessmentId);
    }
}
```

### API client

```java
@Component
@RequiredArgsConstructor
public class ApiClient {

    private final Session session;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();
    private final String baseUrl;

    public <T> T get(String path, Class<T> type) {
        return exchange("GET", path, null, type);
    }

    public <T> T post(String path, Object body, Class<T> type) {
        return exchange("POST", path, body, type);
    }

    public <T> T put(String path, Object body, Class<T> type) {
        return exchange("PUT", path, body, type);
    }

    private <T> T exchange(String method, String path, Object body, Class<T> type) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");

            if (session.getToken() != null) {
                builder.header("Authorization", "Bearer " + session.getToken());
            }

            String json = body == null ? "" : mapper.writeValueAsString(body);
            builder.method(method, HttpRequest.BodyPublishers.ofString(json));

            HttpResponse<String> response = client.send(builder.build(),
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw ApiException.from(response);
            }

            ApiResponse<T> wrapper = mapper.readValue(response.body(),
                mapper.getTypeFactory().constructParametricType(ApiResponse.class, type));
            return wrapper.getData();
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Network error: " + e.getMessage(), e);
        }
    }
}
```

### Toast

```java
public class Toast {

    public static void success(String message) {
        show(message, "toast-success");
    }

    public static void error(String message) {
        show(message, "toast-error");
    }

    public static void info(String message) {
        show(message, "toast-info");
    }

    private static void show(String message, String styleClass) {
        Platform.runLater(() -> {
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initModality(Modality.NONE);

            Label label = new Label(message);
            label.getStyleClass().addAll("toast", styleClass);

            Scene scene = new Scene(new StackPane(label));
            scene.setFill(Color.TRANSPARENT);
            scene.getStylesheets().add(Toast.class.getResource("/css/components.css").toExternalForm());

            stage.setScene(scene);
            stage.setAlwaysOnTop(true);
            stage.show();

            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(e -> stage.close());
            delay.play();
        });
    }
}
```

### TaskRunner

```java
public class TaskRunner {

    public static <T> void run(Callable<T> work, Consumer<T> onSuccess, Consumer<Exception> onError) {
        Task<T> task = new Task<>() {
            @Override protected T call() throws Exception { return work.call(); }
        };
        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
        task.setOnFailed(e -> onError.accept((Exception) task.getException()));

        Thread thread = new Thread(task, "api-task");
        thread.setDaemon(true);
        thread.start();
    }
}
```

### CSS classes

Match the design system. Key classes:

```css
.root { -fx-font-family: "Inter"; -fx-font-size: 14px; -fx-background-color: #F8FAFC; }
.h1 { -fx-font-size: 24px; -fx-font-weight: 600; -fx-text-fill: #0F172A; }
.h2 { -fx-font-size: 20px; -fx-font-weight: 600; }
.card { -fx-background-color: white; -fx-background-radius: 12; -fx-padding: 24; }
.btn-primary { -fx-background-color: #4F46E5; -fx-text-fill: white; -fx-background-radius: 8; }
.btn-secondary { -fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-radius: 8; }
.badge-draft { -fx-background-color: #FFFBEB; -fx-text-fill: #B45309; -fx-background-radius: 999; }
.badge-published { -fx-background-color: #ECFDF5; -fx-text-fill: #047857; -fx-background-radius: 999; }
```

Full CSS in `docs/shared/DESIGN_SYSTEM.md`.

---

## 6. The Mark Entry Grid (Critical)

This is the most important screen. It's what teachers will judge the whole system by.

Requirements:
- TableView with editable cells
- Tab moves to next cell
- Enter commits and moves down
- Arrow keys navigate
- Escape cancels edit
- Auto-save after each commit (< 100ms)
- Live grade preview as you type
- Score validation (0–max)
- Cannot publish with empty scores
- Keyboard-only operation possible
- Alternating row colors (subtle)
- Active row highlighted
- Score column in monospace font, right-aligned
- Grade column with color-coded badge
- Sticky header
- Progress bar at bottom: entered/total

If any of these fail, teachers will hate it.

---

## 7. Testing

### Unit tests for controllers

- JavaFX isn't easily unit-testable
- Test service classes instead
- Mock the API client

### Service tests

```java
@ExtendWith(MockitoExtension.class)
class MarkServiceTest {

    @Mock private TeacherMarkApi api;
    @InjectMocks private MarkService service;

    @Test
    @DisplayName("should return grid when API returns data")
    void shouldReturnGridWhenApiReturnsData() {
        MarkGridResponse expected = new MarkGridResponse();
        when(api.getGrid(1L)).thenReturn(expected);

        MarkGridResponse result = service.loadGrid(1L);

        assertThat(result).isEqualTo(expected);
    }
}
```

### Manual testing

- Test every screen on a real Windows PC
- Test keyboard navigation on the mark entry grid
- Test error states (backend down, permission denied)
- Test slow network (simulate)
- Test the .msi installer on a fresh PC

---

## 8. Packaging

Use `jpackage` to create a Windows `.msi`:

```bash
jpackage \
  --type msi \
  --name "Musomi Manager" \
  --input target/ \
  --main-jar desktop-1.0.0.jar \
  --main-class com.musomi.desktop.DesktopApplication \
  --icon src/main/resources/icons/app-icon.ico \
  --app-version 1.0.0 \
  --vendor "Musomi" \
  --win-shortcut \
  --win-menu
```

Output: `MusomiManager-1.0.0.msi` — double-click to install.

Bundles a JRE. No Java installation needed on teacher PCs.

---

## 9. Common Prompts for JavaFX AI

**Generate an FXML screen:**
```
Using the conventions in DESKTOP.md and the design system in DESIGN_SYSTEM.md,
build FXML for the [screen name] screen.

Purpose: [what it does]
Contains: [list components]
Uses MaterialFX controls. CSS classes from DESIGN_SYSTEM.md.
Controller skeleton with @FXML fields and empty handlers.
```

**Generate a controller:**
```
FXML: [paste FXML]
Service methods available: [list]

Generate the controller.
Rules:
- @FXML annotations on all nodes
- Initialize in initialize()
- Use TaskRunner for API calls (no blocking FX thread)
- Show toasts for success/error
- Log at DEBUG for user actions
Follow the patterns in DESKTOP.md.
```

**Generate the mark entry grid:**
```
Context: DESKTOP.md, DESIGN_SYSTEM.md

Build mark-entry.fxml and MarkEntryController.
Requirements:
- TableView with columns: student name, score (editable), grade (badge), feedback
- Tab moves next cell, Enter commits and moves down
- Auto-save after cell commit
- Live grade calculation using GradeFormatter
- Validation: score between 0 and max
- Progress label at bottom: entered/total
- Save Draft and Publish buttons
- Status badge showing DRAFT or PUBLISHED
Follow the patterns in DESKTOP.md.
```

**Add a form:**
```
Context: DESKTOP.md

Build user-form.fxml and UserFormController for creating/editing a user.
Fields: full name, username, email, phone, role, password.
Validation on save. Toast on success. Modal on error.
Follow the patterns in DESKTOP.md.
```

**Debug a JavaFX issue:**
```
Context: DESKTOP.md
Error: [paste error]
Code: [paste code]

Explain cause, fix, prevention.
```

---

## 10. Definition of Done (Desktop)

A desktop task is done when:

- [ ] FXML matches the design system (colors, fonts, spacing)
- [ ] Controller follows DESKTOP.md patterns
- [ ] API calls use `TaskRunner` — no blocking on FX thread
- [ ] Toast for success, modal for errors
- [ ] Keyboard navigation works (Tab, Enter, Escape)
- [ ] Empty state implemented
- [ ] Loading state implemented (spinner after 200ms)
- [ ] Error state implemented with plain-language message
- [ ] Works at minimum window size 1280×720
- [ ] Reviewed by another team member
- [ ] Tested on real Windows PC

---

## 11. What NOT to Do

- Don't do network calls on the JavaFX thread
- Don't put business logic in controllers — call services
- Don't use `System.out.println` — use SLF4J
- Don't hardcode colors or fonts — use CSS classes
- Don't inline styles in FXML — use CSS
- Don't call the API client directly from controllers — go through services
- Don't skip loading states — always show feedback within 200ms
- Don't swallow exceptions — show a toast and log

---

## 12. Reference Documents

- `MASTER.md` — project context (paste first)
- `docs/shared/API_CONTRACT.md` — endpoints you call
- `docs/shared/SCHEMA.md` — models you display
- `docs/shared/DESIGN_SYSTEM.md` — colors, fonts, components
- `docs/shared/ERROR_CODES.md` — error handling
- `docs/specialists/BACKEND.md` — for API contract details

---

## The One-Sentence Summary

**You are the JavaFX Developer on Musomi Manager. You build the desktop app teachers and admins use — FXML + controllers + services + API client + JavaFX CSS. The mark entry grid is the most important screen in the whole system. Paste MASTER.md and DESKTOP.md into every AI session, then paste the relevant API_CONTRACT.md and DESIGN_SYSTEM.md sections for the task at hand.**
