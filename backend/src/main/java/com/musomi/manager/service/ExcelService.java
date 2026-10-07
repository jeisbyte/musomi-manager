package com.musomi.manager.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.musomi.manager.dto.response.ImportPreviewResponse;
import com.musomi.manager.dto.response.ImportPreviewResponse.ImportRowError;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

/** Parses student spreadsheets and builds import templates. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelService {

    private static final List<String> REQUIRED_HEADERS = List.of(
            "admissionNumber", "fullName", "gender", "dateOfBirth", "currentClassId", "currentStreamId");

    /** Parses and validates student rows from an XLSX workbook. */
    public ImportPreviewResponse parseStudentFile(InputStream inputStream) throws IOException {
        return parseStudentFileWithRows(inputStream).preview();
    }

    ParsedStudentFile parseStudentFileWithRows(InputStream inputStream) throws IOException {
        Workbook workbook;
        try {
            workbook = new XSSFWorkbook(inputStream);
        } catch (IOException | POIXMLException | IllegalArgumentException exception) {
            throw new ValidationException(ErrorCode.INVALID_IMPORT_FILE);
        }

        try (workbook) {
            if (workbook.getNumberOfSheets() == 0 || workbook.getSheetAt(0).getRow(0) == null) {
                throw new ValidationException(ErrorCode.IMPORT_MISSING_COLUMNS, Map.of("list",
                        String.join(", ", REQUIRED_HEADERS)));
            }

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(Locale.ROOT);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Map<String, Integer> columns = readHeaders(sheet.getRow(0), formatter, evaluator);
            List<String> missingHeaders = REQUIRED_HEADERS.stream()
                    .filter(header -> !columns.containsKey(header))
                    .toList();
            if (!missingHeaders.isEmpty()) {
                throw new ValidationException(
                        ErrorCode.IMPORT_MISSING_COLUMNS, Map.of("list", String.join(", ", missingHeaders)));
            }

            List<ImportRowError> errors = new ArrayList<>();
            List<StudentRow> validRows = new ArrayList<>();
            int totalRows = 0;
            int errorRows = 0;
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (isEmptyRow(row, formatter, evaluator)) {
                    continue;
                }
                totalRows++;
                int beforeErrors = errors.size();
                StudentRow studentRow = parseRow(row, rowIndex + 1, columns, formatter, evaluator, errors);
                if (errors.size() == beforeErrors) {
                    validRows.add(studentRow);
                } else {
                    errorRows++;
                }
            }

            ImportPreviewResponse preview = new ImportPreviewResponse(
                    null, totalRows, validRows.size(), errorRows, List.copyOf(errors));
            return new ParsedStudentFile(preview, List.copyOf(validRows));
        }
    }

    /** Builds an XLSX student import template with a sample row. */
    public byte[] buildStudentTemplate() {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Students");
            Row header = sheet.createRow(0);
            for (int index = 0; index < REQUIRED_HEADERS.size(); index++) {
                header.createCell(index).setCellValue(REQUIRED_HEADERS.get(index));
            }
            Row example = sheet.createRow(1);
            example.createCell(0).setCellValue("2024/0001");
            example.createCell(1).setCellValue("Jane Doe");
            example.createCell(2).setCellValue("FEMALE");
            example.createCell(3).setCellValue("2008-01-15");
            example.createCell(4).setCellValue(1);
            example.createCell(5).setCellValue(1);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to build the student import template", exception);
        }
    }

    private Map<String, Integer> readHeaders(Row header, DataFormatter formatter, FormulaEvaluator evaluator) {
        Map<String, Integer> columns = new HashMap<>();
        for (int index = 0; index < header.getLastCellNum(); index++) {
            String name = formatter.formatCellValue(header.getCell(index), evaluator).trim();
            if (!name.isEmpty()) {
                columns.put(name.toLowerCase(Locale.ROOT), index);
            }
        }
        Map<String, Integer> normalizedColumns = new HashMap<>();
        columns.forEach((name, index) -> normalizedColumns.put(
                REQUIRED_HEADERS.stream()
                        .filter(required -> required.equalsIgnoreCase(name))
                        .findFirst()
                        .orElse(name),
                index));
        return normalizedColumns;
    }

    private boolean isEmptyRow(Row row, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (row == null) {
            return true;
        }
        for (int index = 0; index < row.getLastCellNum(); index++) {
            if (!cellValue(row.getCell(index), formatter, evaluator).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private StudentRow parseRow(Row row, int rowNumber, Map<String, Integer> columns,
                                DataFormatter formatter, FormulaEvaluator evaluator,
                                List<ImportRowError> errors) {
        String admissionNumber = value(row, columns, "admissionNumber", formatter, evaluator);
        String fullName = value(row, columns, "fullName", formatter, evaluator);
        String genderValue = value(row, columns, "gender", formatter, evaluator);
        String dateOfBirthValue = dateValue(row.getCell(columns.get("dateOfBirth")), formatter, evaluator);
        String classIdValue = value(row, columns, "currentClassId", formatter, evaluator);
        String streamIdValue = value(row, columns, "currentStreamId", formatter, evaluator);

        if (admissionNumber.isBlank()) {
            errors.add(new ImportRowError(rowNumber, "admissionNumber", "admissionNumber is required"));
        }
        if (fullName.isBlank()) {
            errors.add(new ImportRowError(rowNumber, "fullName", "fullName is required"));
        }

        String gender = genderValue.isBlank() ? null : genderValue.toUpperCase(Locale.ROOT);
        if (gender != null && !List.of("MALE", "FEMALE", "OTHER").contains(gender)) {
            errors.add(new ImportRowError(rowNumber, "gender", "Must be MALE, FEMALE, or OTHER"));
        }

        LocalDate dateOfBirth = null;
        if (!dateOfBirthValue.isBlank()) {
            try {
                dateOfBirth = LocalDate.parse(dateOfBirthValue);
            } catch (DateTimeParseException exception) {
                errors.add(new ImportRowError(rowNumber, "dateOfBirth", "Must be a valid date in YYYY-MM-DD format"));
            }
        }

        Long currentClassId = parseId(classIdValue, rowNumber, "currentClassId", errors);
        Long currentStreamId = parseId(streamIdValue, rowNumber, "currentStreamId", errors);
        return new StudentRow(
                admissionNumber.trim(),
                fullName.trim(),
                gender,
                dateOfBirth,
                currentClassId,
                currentStreamId);
    }

    private Long parseId(String value, int rowNumber, String field, List<ImportRowError> errors) {
        if (value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            errors.add(new ImportRowError(rowNumber, field, field + " must be numeric"));
            return null;
        }
    }

    private String value(Row row, Map<String, Integer> columns, String name,
                         DataFormatter formatter, FormulaEvaluator evaluator) {
        return cellValue(row.getCell(columns.get(name)), formatter, evaluator).trim();
    }

    private String dateValue(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate().toString();
        }
        return cellValue(cell, formatter, evaluator).trim();
    }

    private String cellValue(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        return cell == null ? "" : formatter.formatCellValue(cell, evaluator);
    }

    record ParsedStudentFile(ImportPreviewResponse preview, List<StudentRow> validRows) {
    }

    record StudentRow(
            String admissionNumber,
            String fullName,
            String gender,
            LocalDate dateOfBirth,
            Long currentClassId,
            Long currentStreamId
    ) {
    }
}
