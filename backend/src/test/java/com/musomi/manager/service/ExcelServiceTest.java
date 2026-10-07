package com.musomi.manager.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import com.musomi.manager.dto.response.ImportPreviewResponse;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ValidationException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExcelServiceTest {

    private static final List<String> HEADERS = List.of(
            "admissionNumber", "fullName", "gender", "dateOfBirth", "currentClassId", "currentStreamId");

    private final ExcelService excelService = new ExcelService();

    @Test
    @DisplayName("should parse valid file when headers present")
    void shouldParseValidFileWhenHeadersPresent() throws IOException {
        ImportPreviewResponse response = excelService.parseStudentFile(validWorkbook());

        assertThat(response.importId()).isNull();
        assertThat(response.totalRows()).isEqualTo(1);
        assertThat(response.validRows()).isEqualTo(1);
        assertThat(response.errorRows()).isZero();
        assertThat(response.errors()).isEmpty();
    }

    @Test
    @DisplayName("should throw when required headers missing")
    void shouldThrowWhenRequiredHeadersMissing() throws IOException {
        byte[] workbook = workbook(List.of("admissionNumber", "fullName"), List.of());

        assertThatThrownBy(() -> excelService.parseStudentFile(new ByteArrayInputStream(workbook)))
                .isInstanceOf(ValidationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMPORT_MISSING_COLUMNS);
    }

    @Test
    @DisplayName("should return error rows when field values invalid")
    void shouldReturnErrorRowsWhenFieldValuesInvalid() throws IOException {
        byte[] workbook = workbook(HEADERS, List.of(List.of("A-1", "Alex", "INVALID", "not-a-date", "x", "")));

        ImportPreviewResponse response = excelService.parseStudentFile(new ByteArrayInputStream(workbook));

        assertThat(response.totalRows()).isEqualTo(1);
        assertThat(response.validRows()).isZero();
        assertThat(response.errorRows()).isEqualTo(1);
        assertThat(response.errors())
                .extracting(ImportPreviewResponse.ImportRowError::field)
                .contains("gender", "dateOfBirth", "currentClassId");
    }

    @Test
    @DisplayName("should build template with headers and example row")
    void shouldBuildTemplateWithHeadersAndExampleRow() throws IOException {
        byte[] template = excelService.buildStudentTemplate();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(template))) {
            Sheet sheet = workbook.getSheet("Students");
            Row header = sheet.getRow(0);
            Row example = sheet.getRow(1);

            assertThat(HEADERS)
                    .extracting(headerCell -> header.getCell(HEADERS.indexOf(headerCell)).getStringCellValue())
                    .containsExactlyElementsOf(HEADERS);
            assertThat(example.getCell(0).getStringCellValue()).isEqualTo("2024/0001");
            assertThat(example.getCell(1).getStringCellValue()).isEqualTo("Jane Doe");
            assertThat(example.getCell(2).getStringCellValue()).isEqualTo("FEMALE");
            assertThat(example.getCell(3).getStringCellValue()).isEqualTo("2008-01-15");
        }
    }

    private ByteArrayInputStream validWorkbook() throws IOException {
        return new ByteArrayInputStream(workbook(
                List.of("ADMISSIONNUMBER", "fullName", "gender", "dateOfBirth", "currentClassId", "currentStreamId"),
                List.of(List.of("A-1", "Alex Student", "MALE", "2008-01-15", "1", "2"))));
    }

    private byte[] workbook(List<String> headers, List<List<String>> rows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Students");
            Row header = sheet.createRow(0);
            for (int index = 0; index < headers.size(); index++) {
                header.createCell(index).setCellValue(headers.get(index));
            }
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                Row row = sheet.createRow(rowIndex + 1);
                List<String> values = rows.get(rowIndex);
                for (int column = 0; column < values.size(); column++) {
                    row.createCell(column).setCellValue(values.get(column));
                }
            }
            workbook.write(output);
            return output.toByteArray();
        }
    }
}
