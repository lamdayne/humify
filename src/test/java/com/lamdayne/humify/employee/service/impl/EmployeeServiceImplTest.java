package com.lamdayne.humify.employee.service.impl;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.auth.service.PasswordResetTokenService;
import com.lamdayne.humify.auth.service.RoleAccessService;
import com.lamdayne.humify.branch.entity.Branch;
import com.lamdayne.humify.branch.service.BranchAccessService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyAccessService;
import com.lamdayne.humify.department.entity.Department;
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
import com.lamdayne.humify.employee.validator.EmployeeImportValidator;
import com.lamdayne.humify.employee.validator.EmployeeValidator;
import com.lamdayne.humify.position.entity.Position;
import com.lamdayne.humify.position.service.PositionAccessService;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeServiceImpl Tests")
public class EmployeeServiceImplTest {

    @Mock private UserService userService;
    @Mock private EmployeeMapper employeeMapper;
    @Mock private EmployeeValidator employeeValidator;
    @Mock private RoleAccessService roleAccessService;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private BranchAccessService branchAccessService;
    @Mock private CompanyAccessService companyAccessService;
    @Mock private PositionAccessService positionAccessService;
    @Mock private EmployeeSpecification employeeSpecification;
    @Mock private DepartmentAccessService departmentAccessService;
    @Mock private PasswordResetTokenService passwordResetTokenService;
    @Mock private EmployeeExcelParser employeeExcelParser;
    @Mock private EmployeeExcelProcessor employeeExcelProcessor;
    @Mock private EmployeeImportValidator employeeImportValidator;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Company company;
    private Branch branch;
    private Department department;
    private Position position;
    private Employee employee;
    private EmployeeResponse employeeResponse;

    @BeforeEach
    void setUp() {
        CompanyContext.setCompanyId(10L);
        SecurityContextHolder.setContext(securityContext);

        company = Company.builder().build();
        company.setId(10L);

        branch = Branch.builder().build();
        branch.setId(1L);

        department = Department.builder().build();
        department.setId(2L);

        position = Position.builder().build();
        position.setId(3L);

        employee = Employee.builder()
                .company(company)
                .branch(branch)
                .department(department)
                .position(position)
                .employeeCode("EMP-0001")
                .fullName("John Doe")
                .email("john.doe@company.com")
                .status(EmployeeStatus.PROBATION)
                .build();
        employee.setId(100L);

        employeeResponse = EmployeeResponse.builder()
                .fullName("John Doe")
                .email("john.doe@company.com")
                .employeeCode("EMP-0001")
                .build();
    }

    @AfterEach
    void tearDown() {
        CompanyContext.clear();
        SecurityContextHolder.clearContext();
    }

    // ---- createEmployee ----

    @Test
    @DisplayName("Create employee successfully")
    void createEmployee_success() {
        CreateEmployeeRequest request = mock(CreateEmployeeRequest.class);
        when(request.getEmail()).thenReturn("john.doe@company.com");
        when(request.getBranchId()).thenReturn(1L);
        when(request.getDepartmentId()).thenReturn(2L);
        when(request.getPositionId()).thenReturn(3L);
        when(request.getRoleIds()).thenReturn(List.of(20L));
        when(request.getAvatarUrl()).thenReturn("custom-avatar-url");

        when(employeeRepository.existsByCompanyIdAndEmail(10L, "john.doe@company.com")).thenReturn(false);
        when(companyAccessService.getReferenceById(10L)).thenReturn(company);
        when(branchAccessService.getReferenceById(1L)).thenReturn(branch);
        when(departmentAccessService.getReferenceById(2L)).thenReturn(department);
        when(positionAccessService.getReferenceById(3L)).thenReturn(position);

        when(employeeMapper.toEmployee(request)).thenReturn(employee);
        when(employeeRepository.findFirstByCompanyIdAndEmployeeCodeStartingWithOrderByIdDesc(10L, "EMP-"))
                .thenReturn(Optional.empty()); // Generates EMP-0001
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toEmployeeResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse response = employeeService.createEmployee(request);

        assertThat(response).isNotNull();
        assertThat(response.getFullName()).isEqualTo("John Doe");

        verify(employeeValidator).validateRefs(10L, request);
        verify(userService).save(any(User.class));
        verify(roleAccessService).assignRoles(any(User.class), eq(List.of(20L)));
        verify(passwordResetTokenService).setPasswordNewAccount(eq("john.doe@company.com"), any(), eq("John Doe"));
    }

    @Test
    @DisplayName("Create employee throws exception when email already exists")
    void createEmployee_throwsWhenEmailExists() {
        CreateEmployeeRequest request = mock(CreateEmployeeRequest.class);
        when(request.getEmail()).thenReturn("john.doe@company.com");

        when(employeeRepository.existsByCompanyIdAndEmail(10L, "john.doe@company.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EMPLOYEE_EMAIL_EXISTED));

        verify(employeeRepository, never()).save(any());
    }

    // ---- getAllEmployees ----

    @Test
    @DisplayName("Get all employees successfully")
    void getAllEmployees_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(List.of(employee));

        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(employeeMapper.toEmployeeResponse(employee)).thenReturn(employeeResponse);

        PageResponse<EmployeeResponse> result = employeeService.getAllEmployees(1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getFullName()).isEqualTo("John Doe");
    }

    // ---- getEmployeeById ----

    @Test
    @DisplayName("Get employee by ID successfully")
    void getEmployeeById_success() {
        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toEmployeeResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.getEmployeeById(100L);

        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Get employee by ID throws exception when not found")
    void getEmployeeById_throwsWhenNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(999L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    // ---- updateEmployee ----

    @Test
    @DisplayName("Update employee successfully")
    void updateEmployee_success() {
        UserPrincipal userPrincipal = UserPrincipal.builder().id(1L).email("admin@company.com").build();
        User owner = mock(User.class);
        when(owner.getId()).thenReturn(1L);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(roleAccessService.findAllRoleNames(userPrincipal)).thenReturn(Set.of("COMPANY_ADMIN"));
        when(userService.findByEmail(employee.getEmail())).thenReturn(owner);

        UpdateEmployeeRequest request = mock(UpdateEmployeeRequest.class);
        when(request.getNfcCardUid()).thenReturn("nfc-123");

        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toEmployeeResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.updateEmployee(100L, request);

        assertThat(result).isNotNull();
        verify(employeeValidator).validateNfcCardUid("nfc-123", 100L);
        verify(employeeMapper).updateEmployee(employee, request);
    }

    // ---- transferEmployee ----

    @Test
    @DisplayName("Transfer employee successfully")
    void transferEmployee_success() {
        TransferEmployeeRequest request = mock(TransferEmployeeRequest.class);
        when(request.getBranchId()).thenReturn(5L);
        when(request.getDepartmentId()).thenReturn(6L);
        when(request.getPositionId()).thenReturn(7L);

        Branch newBranch = Branch.builder().build();
        newBranch.setId(5L);

        Department newDepartment = Department.builder().build();
        newDepartment.setId(6L);

        Position newPosition = Position.builder().build();
        newPosition.setId(7L);

        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
        when(branchAccessService.getReferenceById(5L)).thenReturn(newBranch);
        when(departmentAccessService.getReferenceById(6L)).thenReturn(newDepartment);
        when(positionAccessService.getReferenceById(7L)).thenReturn(newPosition);

        employeeService.transferEmployee(100L, request);

        verify(employeeValidator).validateTransfer(10L, request);
        verify(employeeRepository).save(employee);
        assertThat(employee.getBranch()).isEqualTo(newBranch);
        assertThat(employee.getDepartment()).isEqualTo(newDepartment);
        assertThat(employee.getPosition()).isEqualTo(newPosition);
    }

    // ---- updateEmployeeStatus ----

    @Test
    @DisplayName("Update employee status successfully without clearing NFC")
    void updateEmployeeStatus_active() {
        UpdateEmployeeStatusRequest request = mock(UpdateEmployeeStatusRequest.class);
        when(request.getStatus()).thenReturn("ACTIVE");

        employee.setNfcCardUid("nfc-123");
        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));

        employeeService.updateEmployeeStatus(100L, request);

        verify(employeeRepository).save(employee);
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        assertThat(employee.getNfcCardUid()).isEqualTo("nfc-123"); // NFC not cleared
    }

    @Test
    @DisplayName("Update employee status clears NFC when resigned")
    void updateEmployeeStatus_resigned() {
        UpdateEmployeeStatusRequest request = mock(UpdateEmployeeStatusRequest.class);
        when(request.getStatus()).thenReturn("RESIGNED");

        employee.setNfcCardUid("nfc-123");
        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));

        employeeService.updateEmployeeStatus(100L, request);

        verify(employeeRepository).save(employee);
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.RESIGNED);
        assertThat(employee.getNfcCardUid()).isNull(); // NFC cleared
    }

    // ---- updateEmployeePosition ----

    @Test
    @DisplayName("Update employee position successfully")
    void updateEmployeePosition_success() {
        UpdateEmployeePositionRequest request = mock(UpdateEmployeePositionRequest.class);
        when(request.getPositionId()).thenReturn(8L);

        Position newPosition = Position.builder().build();
        newPosition.setId(8L);

        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
        when(positionAccessService.getReferenceById(8L)).thenReturn(newPosition);

        employeeService.updateEmployeePosition(100L, request);

        verify(employeeValidator).validatePosition(10L, request);
        verify(employeeRepository).save(employee);
        assertThat(employee.getPosition()).isEqualTo(newPosition);
    }

    // ---- getByEmployeeCode ----

    @Test
    @DisplayName("Get employee by code successfully")
    void getByEmployeeCode_success() {
        when(employeeRepository.findByEmployeeCode("EMP-0001")).thenReturn(Optional.of(employee));
        when(employeeMapper.toEmployeeResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.getByEmployeeCode("EMP-0001");

        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo("John Doe");
    }

    // ---- importEmployeeFromXlsx ----

    @Test
    @DisplayName("Import employees from Excel successfully")
    void importEmployeeFromXlsx_success() {
        MultipartFile file = mock(MultipartFile.class);
        List<EmployeeImportResponse> errors = new ArrayList<>();
        List<EmployeeImportRowDto> rows = List.of(new EmployeeImportRowDto());

        when(companyAccessService.getReferenceById(10L)).thenReturn(company);
        when(employeeExcelParser.parse(eq(file), anyList())).thenAnswer(inv -> {
            return rows;
        });
        when(employeeImportValidator.validate(eq(rows), eq(10L), anyList())).thenReturn(true);

        List<EmployeeImportResponse> result = employeeService.importEmployeeFromXlsx(file);

        assertThat(result).isEmpty();
        verify(employeeExcelProcessor).processAndSave(rows, company);
    }

    @Test
    @DisplayName("Import employees returns errors when validation fails")
    void importEmployeeFromXlsx_validationFailure() {
        MultipartFile file = mock(MultipartFile.class);
        List<EmployeeImportRowDto> rows = List.of(new EmployeeImportRowDto());

        when(companyAccessService.getReferenceById(10L)).thenReturn(company);
        when(employeeExcelParser.parse(eq(file), anyList())).thenReturn(rows);
        when(employeeImportValidator.validate(eq(rows), eq(10L), anyList())).thenAnswer(inv -> {
            List<EmployeeImportResponse> errs = inv.getArgument(2);
            errs.add(new EmployeeImportResponse());
            return false;
        });

        List<EmployeeImportResponse> result = employeeService.importEmployeeFromXlsx(file);

        assertThat(result).hasSize(1);
        verify(employeeExcelProcessor, never()).processAndSave(any(), any());
    }

    // ---- getEmployeeByEmail ----

    @Test
    @DisplayName("Get employee by email successfully")
    void getEmployeeByEmail_success() {
        when(employeeRepository.findByEmail("john.doe@company.com")).thenReturn(Optional.of(employee));

        Employee result = employeeService.getEmployeeByEmail("john.doe@company.com");

        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo("John Doe");
    }

    // ---- deleteEmployee ----

    @Test
    @DisplayName("Delete employee successfully")
    void deleteEmployee_success() {
        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(100L);

        verify(employeeRepository).delete(employee);
    }

    // ---- filterEmployees ----

    @Test
    @DisplayName("Filter employees successfully")
    @SuppressWarnings("unchecked")
    void filterEmployees_success() {
        Pageable pageable = PageRequest.of(0, 10);
        String[] params = new String[]{"fullName:like:John"};
        Page<Employee> page = new PageImpl<>(List.of(employee));

        Specification<Employee> mockSpec = mock(Specification.class);
        when(employeeSpecification.build(anyList())).thenReturn(mockSpec);
        when(employeeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(employeeMapper.toEmployeeResponse(employee)).thenReturn(employeeResponse);

        PageResponse<EmployeeResponse> result = employeeService.filterEmployees(pageable, params);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("Create employee successfully with null avatar")
    void createEmployee_success_nullAvatar() {
        CreateEmployeeRequest request = mock(CreateEmployeeRequest.class);
        when(request.getEmail()).thenReturn("john.doe@company.com");
        when(request.getBranchId()).thenReturn(1L);
        when(request.getDepartmentId()).thenReturn(2L);
        when(request.getPositionId()).thenReturn(3L);
        when(request.getRoleIds()).thenReturn(List.of(20L));
        when(request.getAvatarUrl()).thenReturn(null); // testing null avatar branch

        when(employeeRepository.existsByCompanyIdAndEmail(10L, "john.doe@company.com")).thenReturn(false);
        when(companyAccessService.getReferenceById(10L)).thenReturn(company);
        when(branchAccessService.getReferenceById(1L)).thenReturn(branch);
        when(departmentAccessService.getReferenceById(2L)).thenReturn(department);
        when(positionAccessService.getReferenceById(3L)).thenReturn(position);

        when(employeeMapper.toEmployee(request)).thenReturn(employee);
        when(employeeRepository.findFirstByCompanyIdAndEmployeeCodeStartingWithOrderByIdDesc(10L, "EMP-"))
                .thenReturn(Optional.empty());
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toEmployeeResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse response = employeeService.createEmployee(request);

        assertThat(response).isNotNull();
        assertThat(employee.getAvatarUrl()).isNotNull(); // should set default avatar
    }

    @Test
    @DisplayName("Create employee increments existing employee code")
    void createEmployee_incrementsCode() {
        CreateEmployeeRequest request = mock(CreateEmployeeRequest.class);
        when(request.getEmail()).thenReturn("john.doe@company.com");
        when(request.getBranchId()).thenReturn(1L);
        when(request.getDepartmentId()).thenReturn(2L);
        when(request.getPositionId()).thenReturn(3L);
        when(request.getRoleIds()).thenReturn(List.of(20L));

        when(employeeRepository.existsByCompanyIdAndEmail(10L, "john.doe@company.com")).thenReturn(false);
        when(companyAccessService.getReferenceById(10L)).thenReturn(company);
        when(branchAccessService.getReferenceById(1L)).thenReturn(branch);
        when(departmentAccessService.getReferenceById(2L)).thenReturn(department);
        when(positionAccessService.getReferenceById(3L)).thenReturn(position);

        when(employeeMapper.toEmployee(request)).thenReturn(employee);

        Employee existingEmployee = Employee.builder().employeeCode("EMP-0023").build();
        when(employeeRepository.findFirstByCompanyIdAndEmployeeCodeStartingWithOrderByIdDesc(10L, "EMP-"))
                .thenReturn(Optional.of(existingEmployee)); // testing increment code branch (should get EMP-0024)

        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toEmployeeResponse(employee)).thenReturn(employeeResponse);

        employeeService.createEmployee(request);

        assertThat(employee.getEmployeeCode()).isEqualTo("EMP-0024");
    }

    @Test
    @DisplayName("Create employee handles non-numeric employee code exception")
    void createEmployee_invalidCodeFallback() {
        CreateEmployeeRequest request = mock(CreateEmployeeRequest.class);
        when(request.getEmail()).thenReturn("john.doe@company.com");
        when(request.getBranchId()).thenReturn(1L);
        when(request.getDepartmentId()).thenReturn(2L);
        when(request.getPositionId()).thenReturn(3L);
        when(request.getRoleIds()).thenReturn(List.of(20L));

        when(employeeRepository.existsByCompanyIdAndEmail(10L, "john.doe@company.com")).thenReturn(false);
        when(companyAccessService.getReferenceById(10L)).thenReturn(company);
        when(branchAccessService.getReferenceById(1L)).thenReturn(branch);
        when(departmentAccessService.getReferenceById(2L)).thenReturn(department);
        when(positionAccessService.getReferenceById(3L)).thenReturn(position);

        when(employeeMapper.toEmployee(request)).thenReturn(employee);

        Employee existingEmployee = Employee.builder().employeeCode("EMP-XYZ").build();
        when(employeeRepository.findFirstByCompanyIdAndEmployeeCodeStartingWithOrderByIdDesc(10L, "EMP-"))
                .thenReturn(Optional.of(existingEmployee)); // testing parsing error branch (should fallback to EMP-0001)

        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toEmployeeResponse(employee)).thenReturn(employeeResponse);

        employeeService.createEmployee(request);

        assertThat(employee.getEmployeeCode()).isEqualTo("EMP-0001");
    }

    @Test
    @DisplayName("Update employee with null NFC card does not validate")
    void updateEmployee_nullNfc() {
        UserPrincipal userPrincipal = UserPrincipal.builder().id(1L).email("admin@company.com").build();
        User owner = mock(User.class);
        when(owner.getId()).thenReturn(1L);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(roleAccessService.findAllRoleNames(userPrincipal)).thenReturn(Set.of("COMPANY_ADMIN"));
        when(userService.findByEmail(employee.getEmail())).thenReturn(owner);

        UpdateEmployeeRequest request = mock(UpdateEmployeeRequest.class);
        when(request.getNfcCardUid()).thenReturn(null); // testing null NFC branch

        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toEmployeeResponse(employee)).thenReturn(employeeResponse);

        employeeService.updateEmployee(100L, request);

        verify(employeeValidator, never()).validateNfcCardUid(any(), anyLong());
    }

    @Test
    @DisplayName("Transfer employee with null position does not update position")
    void transferEmployee_nullPosition() {
        TransferEmployeeRequest request = mock(TransferEmployeeRequest.class);
        when(request.getBranchId()).thenReturn(5L);
        when(request.getDepartmentId()).thenReturn(6L);
        when(request.getPositionId()).thenReturn(null); // testing null position branch

        Branch newBranch = Branch.builder().build();
        newBranch.setId(5L);

        Department newDepartment = Department.builder().build();
        newDepartment.setId(6L);

        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
        when(branchAccessService.getReferenceById(5L)).thenReturn(newBranch);
        when(departmentAccessService.getReferenceById(6L)).thenReturn(newDepartment);

        employeeService.transferEmployee(100L, request);

        assertThat(employee.getBranch()).isEqualTo(newBranch);
        assertThat(employee.getDepartment()).isEqualTo(newDepartment);
        assertThat(employee.getPosition()).isEqualTo(position); // Position should remain unchanged
    }

    @Test
    @DisplayName("Update employee status clears NFC when terminated")
    void updateEmployeeStatus_terminated() {
        UpdateEmployeeStatusRequest request = mock(UpdateEmployeeStatusRequest.class);
        when(request.getStatus()).thenReturn("TERMINATED"); // testing TERMINATED status branch

        employee.setNfcCardUid("nfc-456");
        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));

        employeeService.updateEmployeeStatus(100L, request);

        verify(employeeRepository).save(employee);
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.TERMINATED);
        assertThat(employee.getNfcCardUid()).isNull(); // NFC cleared
    }

    @Test
    @DisplayName("Import employees returns early when Excel parses to empty list")
    void importEmployeeFromXlsx_emptyRows() {
        MultipartFile file = mock(MultipartFile.class);

        when(companyAccessService.getReferenceById(10L)).thenReturn(company);
        when(employeeExcelParser.parse(eq(file), anyList())).thenReturn(Collections.emptyList()); // testing empty rows branch

        List<EmployeeImportResponse> result = employeeService.importEmployeeFromXlsx(file);

        assertThat(result).isEmpty();
        verify(employeeImportValidator, never()).validate(any(), anyLong(), any());
    }
}

