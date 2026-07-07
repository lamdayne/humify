package com.lamdayne.humify.employee.service;

import com.lamdayne.humify.common.excel.dto.ImportResult;
import org.springframework.web.multipart.MultipartFile;

public interface EmployeeImportService {
    ImportResult importExcel(MultipartFile file) ;

}
