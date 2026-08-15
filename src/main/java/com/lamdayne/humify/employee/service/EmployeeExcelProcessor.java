package com.lamdayne.humify.employee.service;

import com.lamdayne.humify.branch.entity.Branch;
import com.lamdayne.humify.branch.service.BranchAccessService;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.department.entity.Department;
import com.lamdayne.humify.department.service.DepartmentAccessService;
import com.lamdayne.humify.employee.dto.internal.EmployeeImportRowDto;
import com.lamdayne.humify.employee.dto.request.CreateEmployeeEducationRequest;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.enums.EmployeeStatus;
import com.lamdayne.humify.employee.enums.Gender;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
import com.lamdayne.humify.position.entity.Position;
import com.lamdayne.humify.position.service.PositionAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
public class EmployeeExcelProcessor {

    private final EmployeeRepository employeeRepository;
    private final BranchAccessService branchAccessService;
    private final PositionAccessService positionAccessService;
    private final DepartmentAccessService departmentAccessService;
    private final EmployeeEducationService employeeEducationService;

    @Transactional
    public void processAndSave(List<EmployeeImportRowDto> rowsDto, Company company) {
        Long companyId = company.getId();

        Map<String, List<EmployeeImportRowDto>> groupedByEmail = new LinkedHashMap<>();
        for (EmployeeImportRowDto dto : rowsDto) {
            groupedByEmail.computeIfAbsent(dto.getEmail().trim().toLowerCase(), k -> new ArrayList<>()).add(dto);
        }

        Map<String, Branch> branchCache = new HashMap<>();
        Map<String, Position> positionCache = new HashMap<>();
        Map<String, Department> departmentCache = new HashMap<>();

        int nextEmpNumber = getNextEmployeeCodeNumber(companyId);
        for (Map.Entry<String, List<EmployeeImportRowDto>> entry : groupedByEmail.entrySet()) {
            List<EmployeeImportRowDto> group = entry.getValue();
            EmployeeImportRowDto first = group.get(0);

            Branch branch = branchCache.computeIfAbsent(first.getBranchName(), bName ->
                    branchAccessService.findByName(bName).orElseGet(() ->
                            branchAccessService.save(Branch.builder()
                                    .name(bName)
                                    .company(company)
                                    .field(company.getField())
                                    .branchCode(UUID.randomUUID().toString())
                                    .build()
                            )
                    )
            );

            String departmentKey = branch.getId() + "_" + first.getDepartmentName();
            Department department = departmentCache.computeIfAbsent(departmentKey, key ->
                    departmentAccessService.save(Department.builder()
                            .name(first.getDepartmentName())
                            .branch(branch)
                            .build()
                    )
            );

            Position position = positionCache.computeIfAbsent(first.getPositionName(), pName ->
                    positionAccessService.save(Position.builder()
                            .name(pName)
                            .company(company)
                            .build()
                    )
            );

            Employee employee = Employee.builder()
                    .company(company)
                    .branch(branch)
                    .department(department)
                    .position(position)
                    .fullName(first.getFullName())
                    .email(first.getEmail())
                    .dateOfBirth(first.getDateOfBirth())
                    .gender(parseGender(first.getGender().toUpperCase()))
                    .startDate(first.getStartDate())
                    .status(parseStatus(first.getStatus().toUpperCase()))
                    .employeeCode(String.format("EMP-%04d", nextEmpNumber++))
                    .avatarUrl("https://res.cloudinary.com/dmzsletu0/image/upload/v1782044934/453178253_471506465671661_2781666950760530985_n_wqklyb.png")
                    .build();

            employee = employeeRepository.save(employee);

            for (EmployeeImportRowDto empImportDto : group) {
                CreateEmployeeEducationRequest request = CreateEmployeeEducationRequest.builder()
                        .degreeLevel(empImportDto.getDegree())
                        .schoolName(empImportDto.getSchoolName())
                        .major(empImportDto.getMajor())
                        .startYear(empImportDto.getStartYear())
                        .endYear(empImportDto.getEndYear())
                        .gpa(empImportDto.getGpa())
                        .build();
                employeeEducationService.createEducation(employee.getId(), request);
            }
        }
    }

    private int getNextEmployeeCodeNumber(Long companyId) {
        return employeeRepository.findFirstByCompanyIdAndEmployeeCodeStartingWithOrderByIdDesc(companyId, "EMP-")
                .map(e -> {
                    try {
                        return Integer.parseInt(e.getEmployeeCode().substring(4)) + 1;
                    } catch (Exception ex) {
                        return 1;
                    }
                }).orElse(1);
    }

    private Gender parseGender(String gender) {
        if (gender == null) return Gender.OTHER;
        try {
            return Gender.valueOf(gender.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Gender.OTHER;
        }
    }

    private EmployeeStatus parseStatus(String status) {
        if (status == null) return EmployeeStatus.ACTIVE;
        try {
            return EmployeeStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EmployeeStatus.ACTIVE;
        }
    }

}
