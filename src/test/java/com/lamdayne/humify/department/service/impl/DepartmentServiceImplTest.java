package com.lamdayne.humify.department.service.impl;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lamdayne.humify.branch.entity.Branch;
import com.lamdayne.humify.branch.service.BranchService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.department.dto.request.CreateDepartmentRequest;
import com.lamdayne.humify.department.dto.request.UpdateDepartmentRequest;
import com.lamdayne.humify.department.dto.response.DepartmentResponse;
import com.lamdayne.humify.department.entity.Department;
import com.lamdayne.humify.department.mapper.DepartmentMapper;
import com.lamdayne.humify.department.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class DepartmentServiceImplTest {

    @Mock
    private DepartmentMapper departmentMapper;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private BranchService branchService;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Branch branch;
    private Department department;
    private DepartmentResponse response;
    private CreateDepartmentRequest createRequest;
    private UpdateDepartmentRequest updateRequest;


    @BeforeEach
    void setup() {

        // =========================
        // Branch
        // =========================

        branch = new Branch();


        // =========================
        // Department
        // =========================

        department = Department.builder()
                .branch(branch)
                .name("IT Department")
                .description("Information Technology")
                .build();


        // =========================
        // DepartmentResponse
        // =========================

        response = DepartmentResponse.builder()
                .id(1L)
                .branchId(1L)
                .name("IT Department")
                .description("Information Technology")
                .build();


        // =========================
        // CreateDepartmentRequest
        // =========================

        createRequest = new CreateDepartmentRequest();

        ReflectionTestUtils.setField(
                createRequest,
                "branchId",
                1L
        );

        ReflectionTestUtils.setField(
                createRequest,
                "name",
                "IT Department"
        );

        ReflectionTestUtils.setField(
                createRequest,
                "description",
                "Information Technology"
        );


        // =========================
        // UpdateDepartmentRequest
        // =========================

        updateRequest = new UpdateDepartmentRequest();

        updateRequest.setBranchId(1L);
        updateRequest.setName("Updated IT Department");
        updateRequest.setDescription("Updated description");
    }


    // =========================================================
    // createDepartment()
    // =========================================================

    @Test
    void createDepartment_success() {

        when(branchService.getBranchById(createRequest.getBranchId()))
                .thenReturn(branch);

        when(departmentMapper.toDepartment(createRequest))
                .thenReturn(department);

        when(departmentRepository.save(department))
                .thenReturn(department);

        when(departmentMapper.toDepartmentResponse(department))
                .thenReturn(response);


        DepartmentResponse result =
                departmentService.createDepartment(createRequest);


        // Kiểm tra kết quả

        assertNotNull(result);

        assertThat(result.getName())
                .isEqualTo("IT Department");

        assertThat(result.getDescription())
                .isEqualTo("Information Technology");


        // Kiểm tra các dependency được gọi

        verify(branchService)
                .getBranchById(createRequest.getBranchId());

        verify(departmentMapper)
                .toDepartment(createRequest);

        verify(departmentRepository)
                .save(department);

        verify(departmentMapper)
                .toDepartmentResponse(department);


        // Kiểm tra branch được set vào department

        assertThat(department.getBranch())
                .isEqualTo(branch);
    }


    // =========================================================
    // getDepartmentByBranchId()
    // =========================================================

    @Test
    void getDepartmentByBranchId_success() {

        Department department2 = Department.builder()
                .branch(branch)
                .name("HR Department")
                .description("Human Resources")
                .build();

        DepartmentResponse response2 =
                DepartmentResponse.builder()
                        .id(2L)
                        .branchId(1L)
                        .name("HR Department")
                        .description("Human Resources")
                        .build();


        List<Department> departments =
                List.of(
                        department,
                        department2
                );


        Page<Department> departmentPage =
                new PageImpl<>(
                        departments,
                        Pageable.ofSize(10),
                        2
                );


        when(departmentRepository.findByBranchId(
                eq(1L),
                any(Pageable.class)
        )).thenReturn(departmentPage);


        when(departmentMapper.toDepartmentResponse(department))
                .thenReturn(response);

        when(departmentMapper.toDepartmentResponse(department2))
                .thenReturn(response2);


        PageResponse<DepartmentResponse> result =
                departmentService.getDepartmentByBranchId(
                        1L,
                        0,
                        10
                );


        // Kiểm tra kết quả

        assertNotNull(result);

        assertThat(result.getPageNo())
                .isEqualTo(0);

        assertThat(result.getPageSize())
                .isEqualTo(10);

        assertThat(result.getTotalPages())
                .isEqualTo(1);

        assertThat(result.getTotalElements())
                .isEqualTo(2);

        assertThat(result.getItems())
                .hasSize(2);


        assertThat(result.getItems().get(0).getName())
                .isEqualTo("IT Department");

        assertThat(result.getItems().get(1).getName())
                .isEqualTo("HR Department");


        // Verify

        verify(departmentRepository)
                .findByBranchId(
                        eq(1L),
                        any(Pageable.class)
                );

        verify(departmentMapper)
                .toDepartmentResponse(department);

        verify(departmentMapper)
                .toDepartmentResponse(department2);
    }


    // =========================================================
    // getDepartmentByBranchId() - không có department
    // =========================================================

    @Test
    void getDepartmentByBranchId_empty() {

        Page<Department> emptyPage =
                new PageImpl<>(
                        List.of(),
                        Pageable.ofSize(10),
                        0
                );


        when(departmentRepository.findByBranchId(
                eq(1L),
                any(Pageable.class)
        )).thenReturn(emptyPage);


        PageResponse<DepartmentResponse> result =
                departmentService.getDepartmentByBranchId(
                        1L,
                        0,
                        10
                );


        assertNotNull(result);

        assertThat(result.getPageNo())
                .isEqualTo(0);

        assertThat(result.getPageSize())
                .isEqualTo(10);

        assertThat(result.getTotalPages())
                .isEqualTo(0);

        assertThat(result.getTotalElements())
                .isEqualTo(0);

        assertThat(result.getItems())
                .isEmpty();


        verify(departmentRepository)
                .findByBranchId(
                        eq(1L),
                        any(Pageable.class)
                );
    }


    // =========================================================
    // updateDepartment()
    // =========================================================

    @Test
    void updateDepartment_success() {

        DepartmentResponse updatedResponse =
                DepartmentResponse.builder()
                        .id(1L)
                        .branchId(1L)
                        .name("Updated IT Department")
                        .description("Updated description")
                        .build();


        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(departmentRepository.save(department))
                .thenReturn(department);

        when(departmentMapper.toDepartmentResponse(department))
                .thenReturn(updatedResponse);


        DepartmentResponse result =
                departmentService.updateDepartment(
                        1L,
                        updateRequest
                );


        // Kiểm tra kết quả

        assertNotNull(result);

        assertThat(result.getName())
                .isEqualTo("Updated IT Department");

        assertThat(result.getDescription())
                .isEqualTo("Updated description");


        // Verify

        verify(departmentRepository)
                .findById(1L);

        verify(departmentMapper)
                .updateDepartment(
                        department,
                        updateRequest
                );

        verify(departmentRepository)
                .save(department);

        verify(departmentMapper)
                .toDepartmentResponse(department);
    }


    // =========================================================
    // updateDepartment() - không tìm thấy department
    // =========================================================

    @Test
    void updateDepartment_notFound() {

        when(departmentRepository.findById(999L))
                .thenReturn(Optional.empty());


        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> departmentService.updateDepartment(
                                999L,
                                updateRequest
                        )
                );


        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.DEPARTMENT_NOT_FOUND);


        verify(departmentRepository)
                .findById(999L);

        verify(departmentRepository, never())
                .save(any());

        verify(departmentMapper, never())
                .updateDepartment(
                        any(),
                        any()
                );

        verify(departmentMapper, never())
                .toDepartmentResponse(any(Department.class));
    }


    // =========================================================
    // getReferenceById()
    // =========================================================

    @Test
    void getReferenceById_success() {

        when(departmentRepository.getReferenceById(1L))
                .thenReturn(department);


        Department result =
                departmentService.getReferenceById(1L);


        assertNotNull(result);

        assertThat(result)
                .isEqualTo(department);


        verify(departmentRepository)
                .getReferenceById(1L);
    }


    // =========================================================
    // getById()
    // =========================================================

    @Test
    void getById_success() {

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));


        Department result =
                departmentService.getById(1L);


        assertNotNull(result);

        assertThat(result.getName())
                .isEqualTo("IT Department");

        assertThat(result.getDescription())
                .isEqualTo("Information Technology");


        verify(departmentRepository)
                .findById(1L);
    }


    // =========================================================
    // getById() - không tìm thấy
    // =========================================================

    @Test
    void getById_notFound() {

        when(departmentRepository.findById(999L))
                .thenReturn(Optional.empty());


        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> departmentService.getById(999L)
                );


        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.DEPARTMENT_NOT_FOUND);


        verify(departmentRepository)
                .findById(999L);
    }


    // =========================================================
    // existsById()
    // =========================================================

    @Test
    void existsById_success() {

        when(departmentRepository.existsById(1L))
                .thenReturn(true);


        boolean result =
                departmentService.existsById(1L);


        assertThat(result)
                .isTrue();


        verify(departmentRepository)
                .existsById(1L);
    }


    // =========================================================
    // existsById() - không tồn tại
    // =========================================================

    @Test
    void existsById_notFound() {

        when(departmentRepository.existsById(999L))
                .thenReturn(false);


        boolean result =
                departmentService.existsById(999L);


        assertThat(result)
                .isFalse();


        verify(departmentRepository)
                .existsById(999L);
    }


    // =========================================================
    // existsByIdAndBranchId()
    // =========================================================

    @Test
    void existsByIdAndBranchId_success() {

        when(departmentRepository.existsByIdAndBranchId(
                1L,
                1L
        )).thenReturn(true);


        boolean result =
                departmentService.existsByIdAndBranchId(
                        1L,
                        1L
                );


        assertThat(result)
                .isTrue();


        verify(departmentRepository)
                .existsByIdAndBranchId(
                        1L,
                        1L
                );
    }


    // =========================================================
    // existsByIdAndBranchId() - không tồn tại
    // =========================================================

    @Test
    void existsByIdAndBranchId_notFound() {

        when(departmentRepository.existsByIdAndBranchId(
                999L,
                999L
        )).thenReturn(false);


        boolean result =
                departmentService.existsByIdAndBranchId(
                        999L,
                        999L
                );


        assertThat(result)
                .isFalse();


        verify(departmentRepository)
                .existsByIdAndBranchId(
                        999L,
                        999L
                );
    }


    // =========================================================
    // findByNameAndBranchId()
    // =========================================================

    @Test
    void findByNameAndBranchId_success() {

        when(departmentRepository.findByNameAndBranchId(
                "IT Department",
                1L
        )).thenReturn(Optional.of(department));


        Optional<Department> result =
                departmentService.findByNameAndBranchId(
                        "IT Department",
                        1L
                );


        assertNotNull(result);

        assertThat(result)
                .isPresent();

        assertThat(result.get().getName())
                .isEqualTo("IT Department");


        verify(departmentRepository)
                .findByNameAndBranchId(
                        "IT Department",
                        1L
                );
    }


    // =========================================================
    // findByNameAndBranchId() - không tìm thấy
    // =========================================================

    @Test
    void findByNameAndBranchId_notFound() {

        when(departmentRepository.findByNameAndBranchId(
                "Unknown Department",
                1L
        )).thenReturn(Optional.empty());


        Optional<Department> result =
                departmentService.findByNameAndBranchId(
                        "Unknown Department",
                        1L
                );


        assertNotNull(result);

        assertThat(result)
                .isEmpty();


        verify(departmentRepository)
                .findByNameAndBranchId(
                        "Unknown Department",
                        1L
                );
    }


    // =========================================================
    // findByBranchId()
    // =========================================================

    @Test
    void findByBranchId_success() {

        Department department2 =
                Department.builder()
                        .branch(branch)
                        .name("HR Department")
                        .description("Human Resources")
                        .build();


        List<Department> departments =
                List.of(
                        department,
                        department2
                );


        when(departmentRepository.findByBranchId(1L))
                .thenReturn(departments);


        List<Department> result =
                departmentService.findByBranchId(1L);


        assertNotNull(result);

        assertThat(result)
                .hasSize(2);

        assertThat(result.get(0).getName())
                .isEqualTo("IT Department");

        assertThat(result.get(1).getName())
                .isEqualTo("HR Department");


        verify(departmentRepository)
                .findByBranchId(1L);
    }


    // =========================================================
    // findByBranchId() - không có dữ liệu
    // =========================================================

    @Test
    void findByBranchId_empty() {

        when(departmentRepository.findByBranchId(999L))
                .thenReturn(List.of());


        List<Department> result =
                departmentService.findByBranchId(999L);


        assertNotNull(result);

        assertThat(result)
                .isEmpty();


        verify(departmentRepository)
                .findByBranchId(999L);
    }


    // =========================================================
    // save()
    // =========================================================

    @Test
    void save_success() {

        when(departmentRepository.save(department))
                .thenReturn(department);


        Department result =
                departmentService.save(department);


        assertNotNull(result);

        assertThat(result)
                .isEqualTo(department);


        verify(departmentRepository)
                .save(department);
    }
}
