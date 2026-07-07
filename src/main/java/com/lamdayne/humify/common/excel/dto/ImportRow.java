package com.lamdayne.humify.common.excel.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImportRow<T> {

    private int rowNumber;

    private T data;

}