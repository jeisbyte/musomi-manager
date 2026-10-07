package com.musomi.manager.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.musomi.manager.dto.response.ImportConfirmResponse;
import com.musomi.manager.dto.response.ImportPreviewResponse;
import com.musomi.manager.entity.ClassEntity;
import com.musomi.manager.entity.ImportJob;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.Student;
import com.musomi.manager.entity.Stream;
import com.musomi.manager.entity.User;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.exception.ValidationException;
import com.musomi.manager.repository.ImportJobRepository;
import com.musomi.manager.repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** Previews and confirms Excel student imports. */
@Service
@Slf4j
public class StudentImportService {

    private static final DateTimeFormatter JOB_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final StudentRepository studentRepository;
    private final ImportJobRepository importJobRepository;
    private final ExcelService excelService;
    private final ObjectMapper objectMapper;

    public StudentImportService(StudentRepository studentRepository, ImportJobRepository importJobRepository,
                                ExcelService excelService, ObjectMapper objectMapper) {
        this.studentRepository = studentRepository;
        this.importJobRepository = importJobRepository;
        this.excelService = excelService;
        this.objectMapper = Objects.requireNonNullElseGet(objectMapper, ObjectMapper::new);
    }

    /** Parses a student spreadsheet and persists a pending preview job. */
    @Transactional
    public ImportPreviewResponse previewImport(Long schoolId, Long userId, MultipartFile file) {
        ExcelService.ParsedStudentFile parsed;
        try {
            byte[] bytes = file.getBytes();
            parsed = excelService.parseStudentFileWithRows(new ByteArrayInputStream(bytes));
        } catch (IOException exception) {
            throw new ValidationException(ErrorCode.INVALID_IMPORT_FILE);
        }

        String jobReference = createJobReference();
        String errorsJson = serializePayload(parsed);
        ImportPreviewResponse preview = parsed.preview();
        ImportJob job = ImportJob.builder()
                .school(School.builder().id(schoolId).build())
                .jobReference(jobReference)
                .importType("STUDENTS")
                .status("PENDING")
                .totalRows(preview.totalRows())
                .validRows(preview.validRows())
                .errorRows(preview.errorRows())
                .errorsJson(errorsJson)
                .originalFilename(file.getOriginalFilename())
                .createdBy(User.builder().id(userId).build())
                .createdAt(LocalDateTime.now())
                .build();
        importJobRepository.save(job);

        log.info("Student import preview created: reference={}, schoolId={}, validRows={}, errorRows={}",
                jobReference, schoolId, preview.validRows(), preview.errorRows());
        return new ImportPreviewResponse(
                jobReference, preview.totalRows(), preview.validRows(), preview.errorRows(), preview.errors());
    }

    /** Imports valid rows from a pending preview job and marks the job confirmed. */
    @Transactional
    public ImportConfirmResponse confirmImport(Long schoolId, String jobReference) {
        ImportJob job = importJobRepository.findByJobReference(jobReference)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED));
        if (!"PENDING".equals(job.getStatus())) {
            throw new ValidationException(ErrorCode.VALIDATION_FAILED);
        }

        List<JsonNode> validRows = readValidRows(job.getErrorsJson());
        int imported = 0;
        int skipped = 0;
        Set<String> admissionsInImport = new HashSet<>();
        for (JsonNode row : validRows) {
            String admissionNumber = requiredText(row, "admissionNumber");
            if (!admissionsInImport.add(admissionNumber)
                    || studentRepository.existsBySchoolIdAndAdmissionNumber(schoolId, admissionNumber)) {
                skipped++;
                continue;
            }
            Student student = Student.builder()
                    .school(School.builder().id(schoolId).build())
                    .admissionNumber(admissionNumber)
                    .fullName(requiredText(row, "fullName"))
                    .gender(optionalText(row, "gender"))
                    .dateOfBirth(optionalDate(row, "dateOfBirth"))
                    .currentClass(optionalLong(row, "currentClassId") == null
                            ? null
                            : ClassEntity.builder().id(optionalLong(row, "currentClassId")).build())
                    .currentStream(optionalLong(row, "currentStreamId") == null
                            ? null
                            : Stream.builder().id(optionalLong(row, "currentStreamId")).build())
                    .status("ACTIVE")
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            studentRepository.save(student);
            imported++;
        }

        job.setStatus("CONFIRMED");
        job.setConfirmedAt(LocalDateTime.now());
        importJobRepository.save(job);
        log.info("Student import confirmed: reference={}, imported={}, skipped={}",
                jobReference, imported, skipped);
        return new ImportConfirmResponse(imported, skipped);
    }

    /** Builds the student import spreadsheet template. */
    public byte[] buildTemplate() {
        return excelService.buildStudentTemplate();
    }

    private String createJobReference() {
        int randomSuffix = ThreadLocalRandom.current().nextInt(10_000);
        return "imp_" + LocalDateTime.now().format(JOB_TIMESTAMP) + "_" + String.format("%04d", randomSuffix);
    }

    private String serializePayload(ExcelService.ParsedStudentFile parsed) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "errors", parsed.preview().errors(),
                    "validRows", parsed.validRows().stream().map(this::toJsonRow).toList()));
        } catch (JacksonException exception) {
            throw new IllegalStateException("Unable to serialize student import preview", exception);
        }
    }

    private Map<String, Object> toJsonRow(ExcelService.StudentRow row) {
        Map<String, Object> jsonRow = new LinkedHashMap<>();
        jsonRow.put("admissionNumber", row.admissionNumber());
        jsonRow.put("fullName", row.fullName());
        jsonRow.put("gender", row.gender());
        jsonRow.put("dateOfBirth", row.dateOfBirth() == null ? null : row.dateOfBirth().toString());
        jsonRow.put("currentClassId", row.currentClassId());
        jsonRow.put("currentStreamId", row.currentStreamId());
        return jsonRow;
    }

    private List<JsonNode> readValidRows(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode validRows = root == null ? null : root.get("validRows");
            if (validRows == null || !validRows.isArray()) {
                throw new ValidationException(ErrorCode.VALIDATION_FAILED);
            }
            return objectMapper.convertValue(validRows,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, JsonNode.class));
        } catch (JacksonException exception) {
            throw new ValidationException(ErrorCode.VALIDATION_FAILED);
        }
    }

    private String requiredText(JsonNode row, String field) {
        JsonNode value = row.get(field);
        if (value == null || value.isNull() || value.asText().isBlank()) {
            throw new ValidationException(ErrorCode.VALIDATION_FAILED);
        }
        return value.asText();
    }

    private String optionalText(JsonNode row, String field) {
        JsonNode value = row.get(field);
        return value == null || value.isNull() || value.asText().isBlank() ? null : value.asText();
    }

    private java.time.LocalDate optionalDate(JsonNode row, String field) {
        String value = optionalText(row, field);
        if (value == null) {
            return null;
        }
        try {
            return java.time.LocalDate.parse(value);
        } catch (RuntimeException exception) {
            throw new ValidationException(ErrorCode.VALIDATION_FAILED);
        }
    }

    private Long optionalLong(JsonNode row, String field) {
        JsonNode value = row.get(field);
        if (value == null || value.isNull() || value.asText().isBlank()) {
            return null;
        }
        try {
            return value.isNumber() ? value.longValue() : Long.valueOf(value.asText());
        } catch (NumberFormatException exception) {
            throw new ValidationException(ErrorCode.VALIDATION_FAILED);
        }
    }
}
