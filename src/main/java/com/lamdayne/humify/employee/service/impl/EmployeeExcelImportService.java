package com.lamdayne.humify.employee.service.impl;

import com.lamdayne.humify.common.excel.util.CellUtil;
import com.lamdayne.humify.common.excel.util.ExcelImportProcessor;
import com.lamdayne.humify.common.excel.dto.ImportResult;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.employee.dto.request.CreateEmployeeRequest;
import com.lamdayne.humify.employee.service.EmployeeImportService;
import com.lamdayne.humify.employee.service.EmployeeService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EmployeeExcelImportService implements EmployeeImportService {


    private record ExcelColumn(String key, String label) {}

    private static final List<ExcelColumn> COLUMNS = List.of(
            new ExcelColumn("branchId", "Chi nhánh"),
            new ExcelColumn("departmentId", "Phòng ban"),
            new ExcelColumn("positionId", "Vị trí"),
            new ExcelColumn("fullName", "Họ và tên"),
            new ExcelColumn("avatarUrl", "Ảnh đại diện"),
            new ExcelColumn("dateOfBirth", "Ngày sinh"),
            new ExcelColumn("gender", "Giới tính"),
            new ExcelColumn("email", "Email"),
            new ExcelColumn("phone", "Số điện thoại"),
            new ExcelColumn("address", "Địa chỉ"),
            new ExcelColumn("startDate", "Ngày bắt đầu"),
            new ExcelColumn("status", "Trạng thái"),
            new ExcelColumn("roleIds", "Vai trò (roleIds)")
    );

    private static final List<String> HEADERS = COLUMNS.stream()
            .map(ExcelColumn::label)
            .toList();

    private final EmployeeService employeeCreationService; // không còn phụ thuộc EmployeeService
    private final Validator validator;

    public ImportResult importExcel(MultipartFile file) {
        return ExcelImportProcessor.<CreateEmployeeRequest>builder(
                        HEADERS,
                        this::mapRow,
                        this::validate,
                        employeeCreationService::createEmployee// gọi qua bean proxy → REQUIRES_NEW hoạt động đúng
                )
                .withHeaderValidator(this::validateHeader)
                .withDuplicateCheck("email", CreateEmployeeRequest::getEmail)
                .withMaxRows(1000)
                .process(file);
    }

    private CreateEmployeeRequest mapRow(Row row) {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setBranchId(CellUtil.getLong(row, 0));
        request.setDepartmentId(CellUtil.getLong(row, 1));
        request.setPositionId(CellUtil.getLong(row, 2));
        request.setFullName(CellUtil.getString(row, 3));
        request.setAvatarUrl(CellUtil.getString(row, 4));
        request.setDateOfBirth(CellUtil.getDate(row, 5));
        request.setGender(CellUtil.getString(row, 6));
        request.setEmail(CellUtil.getString(row, 7));
        request.setPhone(CellUtil.getString(row, 8));
        request.setAddress(CellUtil.getString(row, 9));
        request.setStartDate(CellUtil.getDate(row, 10));
        request.setStatus(CellUtil.getString(row, 11));
        request.setRoleIds(parseRoleIds(CellUtil.getString(row, 12)));
        return request;
    }

    private List<Long> parseRoleIds(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        try {
            return Arrays.stream(value.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .toList();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid roleIds format: " + value);
        }
    }

    private void validate(CreateEmployeeRequest request) {
        Set<ConstraintViolation<CreateEmployeeRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            ConstraintViolation<CreateEmployeeRequest> violation = violations.iterator().next();
            throw new IllegalArgumentException(violation.getPropertyPath() + ": " + violation.getMessage());
        }
    }

    private void validateHeader(Sheet sheet) {
        Row header = sheet.getRow(0);
        if (header == null) {
            throw new AppException(ErrorCode.INVALID_EXCEL_HEADER);
        }
        for (int i = 0; i < COLUMNS.size(); i++) {
            String value = CellUtil.getString(header, i);
            if (!COLUMNS.get(i).label().equals(value)) {
                throw new IllegalArgumentException(
                        "Cột thứ " + (i + 1) + " phải là \"" + COLUMNS.get(i).label()
                                + "\" (" + COLUMNS.get(i).label() + "), nhưng file của bạn đang có \"" + value + "\""
                );
            }
        }
    }
}