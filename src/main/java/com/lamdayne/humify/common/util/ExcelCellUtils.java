package com.lamdayne.humify.common.util;

import org.apache.poi.ss.usermodel.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ExcelCellUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private ExcelCellUtils() {
    }

    public static Object getCellValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                String value = cell.getStringCellValue();
                return value.isBlank() ? null : value.trim();

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate();
                }
                double numValue = cell.getNumericCellValue();
                return (numValue == (long) numValue) ? (long) numValue : numValue;

            case BOOLEAN:
                return cell.getBooleanCellValue();

            case FORMULA:
                return evaluateFormula(cell);

            default:
                return null;
        }
    }

    public static String getCellValueAsString(Cell cell) {
        Object value = getCellValue(cell);
        if (value == null) return null;
        if (value instanceof LocalDate date) return date.toString();
        return String.valueOf(value);
    }

    public static LocalDate getCellValueAsLocalDate(Cell cell) {
        Object value = getCellValue(cell);
        if (value instanceof LocalDate date) return date;

        if (value instanceof String str && !str.isBlank()) {
            try {
                return LocalDate.parse(str.trim(), DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                return null;
            }
        }
        return null;
    }

    public static Integer getCellValueAsInteger(Cell cell) {
        Object value = getCellValue(cell);
        if (value instanceof Long l) return l.intValue();
        if (value instanceof Double d) return d.intValue();
        if (value instanceof String str && !str.isBlank()) {
            try {
                return Integer.parseInt(str.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public static Double getCellValueAsDouble(Cell cell) {
        Object value = getCellValue(cell);
        if (value instanceof Long l) return l.doubleValue();
        if (value instanceof Double d) return d;
        if (value instanceof String str && !str.isBlank()) {
            try {
                return Double.parseDouble(str.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static Object evaluateFormula(Cell cell) {
        try {
            FormulaEvaluator evaluator = cell.getSheet().getWorkbook()
                    .getCreationHelper().createFormulaEvaluator();
            CellValue result = evaluator.evaluate(cell);

            return switch (result.getCellType()) {
                case STRING -> result.getStringValue().trim();
                case NUMERIC -> result.getNumberValue();
                case BOOLEAN -> result.getBooleanValue();
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

}
