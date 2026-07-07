package com.lamdayne.humify.common.excel.util;

import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;

public class ExcelUtil {

    private ExcelUtil() {
    }

    public static Workbook read(MultipartFile file) {


        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.EMPTY_EXCEL_FILE);
        }

        try {
            return WorkbookFactory.create(file.getInputStream());
        } catch (IOException e) {
            throw new AppException(ErrorCode.INVALID_EXCEL_FILE);
        }
    }

}
