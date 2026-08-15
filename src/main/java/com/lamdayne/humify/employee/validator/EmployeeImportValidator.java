package com.lamdayne.humify.employee.validator;

import com.lamdayne.humify.employee.dto.internal.EmployeeImportRowDto;
import com.lamdayne.humify.employee.dto.response.EmployeeImportResponse;
import com.lamdayne.humify.employee.enums.EmployeeExcelColumn;
import com.lamdayne.humify.employee.enums.EmployeeStatus;
import com.lamdayne.humify.employee.enums.Gender;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static com.lamdayne.humify.employee.enums.EmployeeExcelColumn.*;

@Component
@RequiredArgsConstructor
public class EmployeeImportValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private final EmployeeRepository employeeRepository;

    public boolean validate(List<EmployeeImportRowDto> rowsDto, Long companyId, List<EmployeeImportResponse> errors) {
        Set<String> emailInFileSet = new HashSet<>();
        boolean isValid = true;

        for (EmployeeImportRowDto dto : rowsDto) {
            int rowNum = dto.getRowIndex();

            if (isBlank(dto.getEmail())) {
                errors.add(buildErrorImportResponse(rowNum, getHeaderName(EMAIL), "Email can not blank"));
                isValid = false;
            } else if (!EMAIL_PATTERN.matcher(dto.getEmail().trim()).matches()) {
                errors.add(buildErrorImportResponse(rowNum, getHeaderName(EMAIL), "Invalid email format"));
                isValid = false;
            } else {
                String cleanEmail = dto.getEmail().trim();
                if (emailInFileSet.contains(cleanEmail)) {
                    errors.add(buildErrorImportResponse(rowNum, getHeaderName(EMAIL), "Email already exists in file"));
                    isValid = false;
                } else {
                    emailInFileSet.add(cleanEmail);
                }

                if (employeeRepository.existsByCompanyIdAndEmail(companyId, cleanEmail)) {
                    errors.add(buildErrorImportResponse(rowNum, getHeaderName(EMAIL), "Email already exists in company system"));
                    isValid = false;
                }
            }

            if (isBlank(dto.getBranchName())) {
                errors.add(buildErrorImportResponse(rowNum, getHeaderName(BRANCH), "Branch name can not blank"));
                isValid = false;
            }

            if(isBlank(dto.getDepartmentName())) {
                errors.add(buildErrorImportResponse(rowNum, getHeaderName(DEPARTMENT), "Department name can not blank"));
                isValid = false;
            }

            if (isBlank(dto.getFullName())) {
                errors.add(buildErrorImportResponse(rowNum, getHeaderName(FULL_NAME), "Full name can not blank"));
                isValid = false;
            }

            if (isBlank(dto.getPositionName())) {
                errors.add(buildErrorImportResponse(rowNum, getHeaderName(POSITION), "Position name can not blank"));
                isValid = false;
            }

            if (dto.getDateOfBirth() != null && !dto.getDateOfBirth().isBefore(LocalDate.now())) {
                errors.add(buildErrorImportResponse(rowNum, getHeaderName(DATE_OF_BIRTH), "Date of birth can not before now"));
                isValid = false;
            }

            if (!isBlank(dto.getGender())) {
                String genderUpperCase = dto.getGender().trim().toUpperCase();
                List<String> validGenders = Arrays.stream(Gender.values()).map(Enum::toString).toList();
                if (!validGenders.contains(genderUpperCase)) {
                    errors.add(buildErrorImportResponse(rowNum, getHeaderName(GENDER), "Gender must be one of " + validGenders));
                    isValid = false;
                }
            }

            if (dto.getStartDate() == null) {
                errors.add(buildErrorImportResponse(rowNum, getHeaderName(START_DATE), "Start date can not be blank"));
                isValid = false;
            }

            if (!isBlank(dto.getStatus())) {
                try {
                    EmployeeStatus.valueOf(dto.getStatus().trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    errors.add(buildErrorImportResponse(rowNum, getHeaderName(STATUS), "Status must be one of " + Arrays.toString(EmployeeStatus.values())));
                    isValid = false;
                }
            }

            if (isBlank(dto.getDegree())) {
                errors.add(buildErrorImportResponse(rowNum, getHeaderName(DEGREE), "Degree can not be blank"));
                isValid = false;
            }

            if (isBlank(dto.getSchoolName())) {
                errors.add(buildErrorImportResponse(rowNum, getHeaderName(SCHOOL), "School name can not be blank"));
                isValid = false;
            }

            if (isBlank(dto.getMajor())) {
                errors.add(buildErrorImportResponse(rowNum, getHeaderName(MAJOR), "Major can not be blank"));
                isValid = false;
            }

            if (dto.getStartYear() == null) {
                errors.add(buildErrorImportResponse(rowNum, getHeaderName(START_YEAR), "Start year can not be blank"));
                isValid = false;
            }

            if (dto.getEndYear() == null) {
                errors.add(buildErrorImportResponse(rowNum, getHeaderName(END_YEAR), "End year can not be blank"));
                isValid = false;
            }

            if (dto.getGpa() != null && (dto.getGpa() < 0.0 || dto.getGpa() > 10.0)) {
                errors.add(buildErrorImportResponse(rowNum, getHeaderName(GPA), "GPA must be between 0 and 10.0"));
                isValid = false;
            }
        }

        return isValid;
    }

    private boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    private EmployeeImportResponse buildErrorImportResponse(int rowNum, String field, String error) {
        return EmployeeImportResponse.builder()
                .row(rowNum)
                .field(field)
                .error(error)
                .build();
    }

    private String getHeaderName(EmployeeExcelColumn column) {
        return column.getDefaultHeaderName();
    }

}
