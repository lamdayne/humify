package com.lamdayne.humify.employee.parser;

import com.lamdayne.humify.common.util.ExcelCellUtils;
import com.lamdayne.humify.common.util.ExcelRowUtils;
import com.lamdayne.humify.employee.dto.internal.EmployeeImportRowDto;
import com.lamdayne.humify.employee.dto.response.EmployeeImportResponse;
import com.lamdayne.humify.employee.enums.EmployeeExcelColumn;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Component
public class EmployeeExcelParser {

    public List<EmployeeImportRowDto> parse(MultipartFile file, List<EmployeeImportResponse> errors) {
        List<EmployeeImportRowDto> rowsData = new ArrayList<>();

        try (
                InputStream is = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(is)
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (!rowIterator.hasNext()) {
                errors.add(EmployeeImportResponse.builder()
                        .row(0)
                        .field("File")
                        .error("File is empty")
                        .build()
                );
            }

            Row headerRow = rowIterator.next();
            Map<EmployeeExcelColumn, Integer> columnMap = parseHeaderColumnMap(headerRow);

            int rowIndex = 1;
            while (rowIterator.hasNext()) {
                rowIndex++;
                Row row = rowIterator.next();
                if (ExcelRowUtils.isRowEmpty(row)) continue;

                EmployeeImportRowDto rowDto = parseRow(row, rowIndex, columnMap);
                rowsData.add(rowDto);
            }
        } catch (Exception e) {
            errors.add(EmployeeImportResponse.builder()
                    .row(0)
                    .field("File")
                    .error("Error reading file" + e.getMessage())
                    .build()
            );
        }

        return rowsData;
    }

    private Map<EmployeeExcelColumn, Integer> parseHeaderColumnMap(Row headerRow) {
        Map<EmployeeExcelColumn, Integer> map = new HashMap<>();

        for (Cell cell : headerRow) {
            String cellVal = ExcelCellUtils.getCellValueAsString(cell);
            if (cellVal != null && !cellVal.isBlank()) {
                for (EmployeeExcelColumn col : EmployeeExcelColumn.values()) {
                    if (!map.containsKey(col) && col.matches(cellVal)) {
                        map.put(col, col.getDefaultIndex());
                        break;
                    }
                }
            }
        }

        for (EmployeeExcelColumn col : EmployeeExcelColumn.values()) {
            map.putIfAbsent(col, col.getDefaultIndex());
        }

        return map;
    }

    private EmployeeImportRowDto parseRow(Row row, int rowIndex, Map<EmployeeExcelColumn, Integer> columnMap) {
        EmployeeImportRowDto dto = new EmployeeImportRowDto();
        dto.setRowIndex(rowIndex);
        dto.setBranchName(getStringValue(row, columnMap, EmployeeExcelColumn.BRANCH));
        dto.setDepartmentName(getStringValue(row, columnMap, EmployeeExcelColumn.DEPARTMENT));
        dto.setEmail(getStringValue(row, columnMap, EmployeeExcelColumn.EMAIL));
        dto.setFullName(getStringValue(row, columnMap, EmployeeExcelColumn.FULL_NAME));
        dto.setPositionName(getStringValue(row, columnMap, EmployeeExcelColumn.POSITION));
        dto.setDateOfBirth(ExcelCellUtils.getCellValueAsLocalDate(getCell(row, columnMap, EmployeeExcelColumn.DATE_OF_BIRTH)));
        dto.setGender(getStringValue(row, columnMap, EmployeeExcelColumn.GENDER));
        dto.setStartDate(ExcelCellUtils.getCellValueAsLocalDate(getCell(row, columnMap, EmployeeExcelColumn.START_DATE)));
        dto.setStatus(getStringValue(row, columnMap, EmployeeExcelColumn.STATUS));
        dto.setDegree(getStringValue(row, columnMap, EmployeeExcelColumn.DEGREE));
        dto.setSchoolName(getStringValue(row, columnMap, EmployeeExcelColumn.SCHOOL));
        dto.setMajor(getStringValue(row, columnMap, EmployeeExcelColumn.MAJOR));
        dto.setStartYear(ExcelCellUtils.getCellValueAsInteger(getCell(row, columnMap, EmployeeExcelColumn.START_YEAR)));
        dto.setEndYear(ExcelCellUtils.getCellValueAsInteger(getCell(row, columnMap, EmployeeExcelColumn.END_YEAR)));
        dto.setGpa(ExcelCellUtils.getCellValueAsDouble(getCell(row, columnMap, EmployeeExcelColumn.GPA)));
        return dto;
    }

    private Cell getCell(Row row, Map<EmployeeExcelColumn, Integer> columnMap, EmployeeExcelColumn column) {
        Integer colIndex = columnMap.get(column);
        if (colIndex == null) colIndex = column.getDefaultIndex();
        return row.getCell(colIndex);
    }

    private String getStringValue(Row row, Map<EmployeeExcelColumn, Integer> columnMap, EmployeeExcelColumn column) {
        Cell cell = getCell(row, columnMap, column);
        return ExcelCellUtils.getCellValueAsString(cell);
    }

}
