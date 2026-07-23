package com.lamdayne.humify.employee.service.impl;

import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.auth.service.PasswordResetTokenService;
import com.lamdayne.humify.auth.service.RoleAccessService;
import com.lamdayne.humify.branch.entity.Branch;
import com.lamdayne.humify.branch.service.BranchAccessService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.util.ExcelCellUtils;
import com.lamdayne.humify.common.util.ExcelRowUtils;
import com.lamdayne.humify.common.util.PageableUtil;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyAccessService;
import com.lamdayne.humify.department.entity.Department;
import com.lamdayne.humify.department.service.DepartmentAccessService;
import com.lamdayne.humify.employee.dto.request.*;
import com.lamdayne.humify.employee.dto.response.EmployeeImportResponse;
import com.lamdayne.humify.employee.dto.response.EmployeeResponse;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.enums.EmployeeStatus;
import com.lamdayne.humify.employee.enums.Gender;
import com.lamdayne.humify.employee.mapper.EmployeeMapper;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
import com.lamdayne.humify.employee.service.EmployeeEducationService;
import com.lamdayne.humify.employee.service.EmployeeService;
import com.lamdayne.humify.employee.validator.EmployeeValidator;
import com.lamdayne.humify.position.entity.Position;
import com.lamdayne.humify.position.service.PositionAccessService;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.enums.PasswordFlag;
import com.lamdayne.humify.user.service.UserService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final UserService userService;
    private final EmployeeMapper employeeMapper;
    private final EmployeeValidator employeeValidator;
    private final RoleAccessService roleAccessService;
    private final EmployeeRepository employeeRepository;
    private final BranchAccessService branchAccessService;
    private final CompanyAccessService companyAccessService;
    private final PositionAccessService positionAccessService;
    private final DepartmentAccessService departmentAccessService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmployeeEducationService employeeEducationService;

    @Override
    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        Long companyId = CompanyContext.getCompanyId();

        if (employeeRepository.existsByCompanyIdAndEmail(companyId, request.getEmail())) {
            throw new AppException(ErrorCode.EMPLOYEE_EMAIL_EXISTED);
        }

        employeeValidator.validateRefs(companyId, request);

        Company company = companyAccessService.getReferenceById(companyId);

        Employee employee = employeeMapper.toEmployee(request);
        employee.setCompany(company);
        employee.setBranch(branchAccessService.getReferenceById(request.getBranchId()));
        employee.setDepartment(departmentAccessService.getReferenceById(request.getDepartmentId()));
        employee.setPosition(positionAccessService.getReferenceById(request.getPositionId()));

        if (request.getAvatarUrl() == null) {
            employee.setAvatarUrl("https://res.cloudinary.com/dmzsletu0/image/upload/v1782044934/453178253_471506465671661_2781666950760530985_n_wqklyb.png");
        }

        String employeeCode = generateNextEmployeeCode(companyId);
        employee.setEmployeeCode(employeeCode);

        employee = employeeRepository.save(employee);

        User user = User.builder()
                .company(company)
                .employee(employee)
                .email(employee.getEmail())
                .password(PasswordFlag.PENDING_ACTIVATION.name())
                .active(false)
                .build();

        userService.save(user);

        roleAccessService.assignRoles(user, request.getRoleIds());

        passwordResetTokenService.setPasswordNewAccount(user.getEmail(), user.getId(), employee.getFullName());

        return employeeMapper.toEmployeeResponse(employee);
    }

    @Override
    public PageResponse<EmployeeResponse> getAllEmployees(int page, int size, String... sorts) {
        Pageable pageable = PageableUtil.buildPageable(page, size, sorts);
        Page<Employee> employeePage = employeeRepository.findAll(pageable);

        List<EmployeeResponse> employees = employeePage.stream()
                .map(employeeMapper::toEmployeeResponse)
                .toList();

        return PageResponse.<EmployeeResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalElements(employeePage.getTotalElements())
                .totalPages(employeePage.getTotalPages())
                .items(employees)
                .build();
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        employeeMapper.updateEmployee(employee, request);

        return employeeMapper.toEmployeeResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public void transferEmployee(Long id, TransferEmployeeRequest request) {
        Long companyId = CompanyContext.getCompanyId();
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        employeeValidator.validateTransfer(companyId, request);

        employee.setBranch(branchAccessService.getReferenceById(request.getBranchId()));
        employee.setDepartment(departmentAccessService.getReferenceById(request.getDepartmentId()));

        employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public void updateEmployeeStatus(Long id, UpdateEmployeeStatusRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        employee.setStatus(EmployeeStatus.valueOf(request.getStatus()));

        employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public void updateEmployeePosition(Long id, UpdateEmployeePositionRequest request) {
        Long companyId = CompanyContext.getCompanyId();
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        employeeValidator.validatePosition(companyId, request);
        employee.setPosition(positionAccessService.getReferenceById(request.getPositionId()));

        employeeRepository.save(employee);
    }

    @Override
    public EmployeeResponse getByEmployeeCode(String employeeCode) {
        Employee employee = employeeRepository.findByEmployeeCode(employeeCode).orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        return employeeMapper.toEmployeeResponse(employee);
    }

    @Override
    @Transactional
    public List<EmployeeImportResponse> importEmployeeFromXlsx(MultipartFile xlsxFile) {
        List<EmployeeImportResponse> responses = new ArrayList<>();
        Long companyId = CompanyContext.getCompanyId();
        Company company = companyAccessService.getReferenceById(companyId);

        Map<String, List<RowData>> employeeGroups = new LinkedHashMap<>();

        try (
                InputStream is = xlsxFile.getInputStream();
                Workbook workbook = new XSSFWorkbook(is);
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            if (rows.hasNext()) {
                rows.next();
            }

            int rowIdx = 1;
            while (rows.hasNext()) {
                rowIdx++;
                Row currentRow = rows.next();
                if (ExcelRowUtils.isRowEmpty(currentRow)) {
                    continue;
                }

                RowData data = parseRow(currentRow, rowIdx, responses);
                if (data == null) continue;

                employeeGroups.computeIfAbsent(data.email, k -> new ArrayList<>()).add(data);
            }

            if (!responses.isEmpty()) {
                return responses;
            }

            // Caches in memory to minimize database roundtrips
            Map<String, Branch> branchCache = new HashMap<>();
            Map<String, Department> departmentCache = new HashMap<>();
            Map<String, Position> positionCache = new HashMap<>();

            // Pre-calculate starting employee code number to avoid running DB query for every row
            final String codePrefix = "EMP-";
            Optional<Employee> lastEmployeeOpt = employeeRepository
                    .findFirstByCompanyIdAndEmployeeCodeStartingWithOrderByIdDesc(companyId, codePrefix);
            int nextEmpNumber = 1;
            if (lastEmployeeOpt.isPresent()) {
                String lastCode = lastEmployeeOpt.get().getEmployeeCode();
                try {
                    String numericPart = lastCode.substring(codePrefix.length());
                    nextEmpNumber = Integer.parseInt(numericPart) + 1;
                } catch (Exception ignored) {}
            }
            int currentEmpNumber = nextEmpNumber;

            // Save records
            for (Map.Entry<String, List<RowData>> entry : employeeGroups.entrySet()) {
                String email = entry.getKey();
                List<RowData> groupRows = entry.getValue();
                RowData firstRow = groupRows.get(0);

                // Double check duplicate email in DB
                if (employeeRepository.existsByCompanyIdAndEmail(companyId, email)) {
                    responses.add(new EmployeeImportResponse(firstRow.rowNum, "Email", "Email already exists in company database: " + email));
                    continue;
                }

                // Resolve/Auto-create Branch via Cache
                final String bName = firstRow.branchName;
                Branch branch = branchCache.get(bName);
                if (branch == null) {
                    branch = branchAccessService.findByName(bName)
                            .orElseGet(() -> {
                                Branch newBranch = Branch.builder()
                                        .name(bName)
                                        .company(company)
                                        .field(company.getField())
                                        .branchCode(java.util.UUID.randomUUID().toString())
                                        .build();
                                return branchAccessService.save(newBranch);
                            });
                    branchCache.put(bName, branch);
                }

                // Resolve/Auto-create Department via Cache
                final String dName = firstRow.departmentName;
                final String deptKey = branch.getId() + "_" + dName;
                Department department = departmentCache.get(deptKey);
                if (department == null) {
                    final Branch currentBranch = branch;
                    department = departmentAccessService.findByNameAndBranchId(dName, branch.getId())
                            .orElseGet(() -> {
                                Department newDept = Department.builder()
                                        .name(dName)
                                        .branch(currentBranch)
                                        .build();
                                return departmentAccessService.save(newDept);
                            });
                    departmentCache.put(deptKey, department);
                }

                // Resolve/Auto-create Position via Cache
                final String pName = firstRow.positionName;
                Position position = positionCache.get(pName);
                if (position == null) {
                    position = positionAccessService.findByName(pName)
                            .orElseGet(() -> {
                                Position newPos = Position.builder()
                                        .name(pName)
                                        .company(company)
                                        .build();
                                return positionAccessService.save(newPos);
                            });
                    positionCache.put(pName, position);
                }

                // Create Employee (User login is automatically linked on Google OAuth)
                Employee employee = Employee.builder()
                        .company(company)
                        .branch(branch)
                        .department(department)
                        .position(position)
                        .fullName(firstRow.fullName)
                        .email(firstRow.email)
                        .dateOfBirth(firstRow.dateOfBirth)
                        .gender(Gender.valueOf(firstRow.gender))
                        .startDate(firstRow.startDate)
                        .status(EmployeeStatus.valueOf(firstRow.status))
                        .employeeCode(codePrefix + (currentEmpNumber++))
                        .avatarUrl("https://res.cloudinary.com/dmzsletu0/image/upload/v1782044934/453178253_471506465671661_2781666950760530985_n_wqklyb.png")
                        .build();

                employee = employeeRepository.save(employee);

                // Save related Educations for this Employee
                for (RowData r : groupRows) {
                    if (r.schoolName != null && !r.schoolName.isBlank()) {
                        CreateEmployeeEducationRequest eduReq = new CreateEmployeeEducationRequest();
                        eduReq.setDegreeLevel(r.degree != null && !r.degree.isBlank() ? r.degree : "Bachelor");
                        eduReq.setSchoolName(r.schoolName);
                        eduReq.setMajor(r.major);
                        eduReq.setStartYear(r.startYear);
                        eduReq.setEndYear(r.endYear);
                        eduReq.setGpa(r.gpa);
                        employeeEducationService.createEducation(employee.getId(), eduReq);
                    }
                }
            }
            
        } catch (Exception e) {
            responses.add(new EmployeeImportResponse(0, "File", "Could not read Excel file: " + e.getMessage()));
        }

        return responses;
    }

    private String generateNextEmployeeCode(Long companyId) {
        final String prefix = "EMP-";

        Optional<Employee> lastEmployeeOpt = employeeRepository
                .findFirstByCompanyIdAndEmployeeCodeStartingWithOrderByIdDesc(companyId, prefix);

        int nextNumber = 1;
        if (lastEmployeeOpt.isPresent()) {
            String lastCode = lastEmployeeOpt.get().getEmployeeCode();
            try {
                String numericPart = lastCode.substring(prefix.length());
                nextNumber = Integer.parseInt(numericPart) + 1;
            } catch (Exception e) {
                nextNumber = 1;
            }
        }
        return String.format("%s%04d", prefix, nextNumber);
    }

    @Override
    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    private static class RowData {
        int rowNum;
        String branchName;
        String departmentName;
        String email;
        String fullName;
        String positionName;
        LocalDate dateOfBirth;
        String gender;
        LocalDate startDate;
        String status;
        String degree;
        String schoolName;
        String major;
        Integer startYear;
        Integer endYear;
        Double gpa;
    }

    private RowData parseRow(Row row, int rowIdx, List<EmployeeImportResponse> responses) {
        RowData data = new RowData();
        data.rowNum = rowIdx;

        try {
            data.branchName = ExcelCellUtils.getCellValueAsString(row.getCell(0));
            data.departmentName = ExcelCellUtils.getCellValueAsString(row.getCell(1));
            data.email = ExcelCellUtils.getCellValueAsString(row.getCell(2));
            data.fullName = ExcelCellUtils.getCellValueAsString(row.getCell(3));
            data.positionName = ExcelCellUtils.getCellValueAsString(row.getCell(4));
            data.dateOfBirth = ExcelCellUtils.getCellValueAsLocalDate(row.getCell(5));
            String genderStr = ExcelCellUtils.getCellValueAsString(row.getCell(6));
            if (genderStr != null && !genderStr.isBlank()) {
                String upper = genderStr.trim().toUpperCase();
                if (upper.equals("MALE") || upper.equals("NAM")) {
                    data.gender = "MALE";
                } else if (upper.equals("FEMALE") || upper.equals("NỮ") || upper.equals("NU")) {
                    data.gender = "FEMALE";
                } else if (upper.equals("OTHER")) {
                    data.gender = "OTHER";
                } else {
                    data.gender = "MALE";
                }
            } else {
                data.gender = "MALE";
            }

            data.startDate = ExcelCellUtils.getCellValueAsLocalDate(row.getCell(7));
            if (data.startDate == null) {
                data.startDate = LocalDate.now();
            }

            String statusStr = ExcelCellUtils.getCellValueAsString(row.getCell(8));
            if (statusStr != null && !statusStr.isBlank()) {
                String upper = statusStr.trim().toUpperCase();
                try {
                    EmployeeStatus.valueOf(upper);
                    data.status = upper;
                } catch (IllegalArgumentException e) {
                    data.status = "ACTIVE";
                }
            } else {
                data.status = "ACTIVE";
            }
            data.degree = ExcelCellUtils.getCellValueAsString(row.getCell(9));
            data.schoolName = ExcelCellUtils.getCellValueAsString(row.getCell(10));
            data.major = ExcelCellUtils.getCellValueAsString(row.getCell(11));
            data.startYear = ExcelCellUtils.getCellValueAsInteger(row.getCell(12));
            data.endYear = ExcelCellUtils.getCellValueAsInteger(row.getCell(13));
            data.gpa = ExcelCellUtils.getCellValueAsDouble(row.getCell(14));

            validateRowData(data, responses);
        } catch (Exception e) {
            responses.add(new EmployeeImportResponse(
                    rowIdx, "Row Data", "Invalid formats on row: " + e.getMessage())
            );
            return null;
        }
        return data;
    }

    private void validateRowData(RowData data, List<EmployeeImportResponse> responses) {
        int rowIdx = data.rowNum;
        if (data.email == null || data.email.isBlank()) {
            responses.add(new EmployeeImportResponse(rowIdx, "Email", "Email is required"));
            return;
        }
        if (data.fullName == null || data.fullName.isBlank()) {
            responses.add(new EmployeeImportResponse(rowIdx, "Full name", "Full name is required"));
            return;
        }
        if (data.branchName == null || data.branchName.isBlank()) {
            responses.add(new EmployeeImportResponse(rowIdx, "Branch", "Branch name is required"));
            return;
        }
        if (data.departmentName == null || data.departmentName.isBlank()) {
            responses.add(new EmployeeImportResponse(rowIdx, "Department", "Department name is required"));
            return;
        }
        if (data.positionName == null || data.positionName.isBlank()) {
            responses.add(new EmployeeImportResponse(rowIdx, "Position", "Position name is required"));
        }
    }

}
