package com.lamdayne.humify.common.excel.dto;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ImportResult {

    private int totalRows;

    private int successRows;

    private int failedRows;

    private final List<ImportError> errors = new ArrayList<>();

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public void increaseSuccess() {
        successRows++;
    }

    public void addError(int row, String field, String message) {
        failedRows++;
        errors.add(
                ImportError.builder()
                        .row(row)
                        .field(field)
                        .message(message)
                        .build()
        );
    }
}