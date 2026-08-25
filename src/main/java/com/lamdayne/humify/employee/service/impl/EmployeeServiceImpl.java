package com.lamdayne.humify.employee.service.impl;

import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.auth.service.PasswordResetTokenService;
import com.lamdayne.humify.auth.service.RoleAccessService;
import com.lamdayne.humify.branch.service.BranchAccessService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.search.SearchCriteriaParser;
import com.lamdayne.humify.common.search.SpecSearchCriteria;
import com.lamdayne.humify.common.util.PageableUtil;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyAccessService;
import com.lamdayne.humify.department.service.DepartmentAccessService;
import com.lamdayne.humify.employee.dto.internal.EmployeeImportRowDto;
import com.lamdayne.humify.employee.dto.request.*;
import com.lamdayne.humify.employee.dto.response.EmployeeImportResponse;
import com.lamdayne.humify.employee.dto.response.EmployeeResponse;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.enums.EmployeeStatus;
import com.lamdayne.humify.employee.mapper.EmployeeMapper;
import com.lamdayne.humify.employee.parser.EmployeeExcelParser;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
import com.lamdayne.humify.employee.repository.EmployeeSpecification;
import com.lamdayne.humify.employee.service.EmployeeExcelProcessor;
import com.lamdayne.humify.employee.service.EmployeeService;
import com.lamdayne.humify.employee.validator.EmployeeImportValidator;
import com.lamdayne.humify.employee.validator.EmployeeValidator;
import com.lamdayne.humify.position.service.PositionAccessService;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.enums.PasswordFlag;
import com.lamdayne.humify.user.service.UserService;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
    private final EmployeeSpecification employeeSpecification;
    private final DepartmentAccessService departmentAccessService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmployeeExcelParser employeeExcelParser;
    private final EmployeeExcelProcessor employeeExcelProcessor;
    private final EmployeeImportValidator employeeImportValidator;

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
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        return employeeMapper.toEmployeeResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        if (request.getNfcCardUid() != null && !request.getNfcCardUid().isBlank()) {
            employeeValidator.validateNfcCardUid(request.getNfcCardUid(), id);
        }

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

        if (request.getPositionId() != null) {
            employee.setPosition(positionAccessService.getReferenceById(request.getPositionId()));
        }

        employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public void updateEmployeeStatus(Long id, UpdateEmployeeStatusRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        EmployeeStatus newStatus = EmployeeStatus.valueOf(request.getStatus());
        employee.setStatus(newStatus);

        if (newStatus == EmployeeStatus.RESIGNED || newStatus == EmployeeStatus.TERMINATED) {
            employee.setNfcCardUid(null);
            log.info("Cleared NFC card UID for employee id={} because status changed to {}", id, newStatus);
        }

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
        List<EmployeeImportResponse> errors = new ArrayList<>();
        Long companyId = CompanyContext.getCompanyId();
        Company company = companyAccessService.getReferenceById(companyId);

        List<EmployeeImportRowDto> rowsDto = employeeExcelParser.parse(xlsxFile, errors);
        if (rowsDto.isEmpty()) return errors;

        boolean isValid = employeeImportValidator.validate(rowsDto, companyId, errors);
        if (!isValid) return errors;

        employeeExcelProcessor.processAndSave(rowsDto, company);
        return errors;
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

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        employeeRepository.delete(employee);
    }

    @Override
    public PageResponse<EmployeeResponse> filterEmployees(Pageable pageable, String[] params) {
        List<SpecSearchCriteria> criteriaList = SearchCriteriaParser.parse(params);
        Specification<Employee> specification = employeeSpecification.build(criteriaList);
        Page<Employee> employeePage = employeeRepository.findAll(specification, pageable);
        List<EmployeeResponse> responses = employeePage.stream()
                .map(employeeMapper::toEmployeeResponse)
                .toList();
        return PageResponse.<EmployeeResponse>builder()
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements(employeePage.getTotalElements())
                .totalPages(employeePage.getTotalPages())
                .items(responses)
                .build();
    }

    @Override
    public Employee getEmployeeEntityByIdAndCompanyId(
            Long employeeId,
            Long companyId
    ) {
        return employeeRepository
                .findByIdAndCompanyIdAndDeletedAtIsNull(
                        employeeId,
                        companyId
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.EMPLOYEE_NOT_FOUND
                        )
                );
    }

}
