package com.lamdayne.humify.common.excel.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DateUtil;

import java.time.LocalDate;

public class CellUtil {

    private CellUtil() {
    }

    public static String getString(Row row, int index) {

        Cell cell = row.getCell(index);

        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {

            case STRING ->
                    cell.getStringCellValue().trim();

            case NUMERIC ->
                    DateUtil.isCellDateFormatted(cell)
                            ? cell.getLocalDateTimeCellValue().toLocalDate().toString()
                            : String.valueOf((long) cell.getNumericCellValue());

            case BOOLEAN ->
                    String.valueOf(cell.getBooleanCellValue());

            case FORMULA ->
                    cell.getCellFormula();

            default ->
                    null;
        };
    }

    public static Long getLong(Row row, int index) {

        String value = getString(row, index);

        if (value == null || value.isBlank()) {
            return null;
        }

        return Long.parseLong(value);
    }

    public static LocalDate getDate(Row row, int index) {

        Cell cell = row.getCell(index);

        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC
                && DateUtil.isCellDateFormatted(cell)) {

            return cell.getLocalDateTimeCellValue().toLocalDate();
        }

        String value = getString(row, index);

        if (value == null || value.isBlank()) {
            return null;
        }

        return LocalDate.parse(value);
    }

    public static Boolean getBoolean(Row row, int index) {

        String value = getString(row, index);

        if (value == null || value.isBlank()) {
            return null;
        }

        return Boolean.parseBoolean(value);
    }

}