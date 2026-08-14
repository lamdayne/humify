package com.lamdayne.humify.branch.service.impl;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.branch.dto.request.CreateBranchRequest;
import com.lamdayne.humify.branch.dto.request.UpdateBranchRequest;
import com.lamdayne.humify.branch.dto.response.BranchResponse;
import com.lamdayne.humify.branch.entity.Branch;
import com.lamdayne.humify.branch.enums.BranchStatus;
import com.lamdayne.humify.branch.mapper.BranchMapper;
import com.lamdayne.humify.branch.repository.BranchRepository;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyService;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchServiceImplTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private CompanyService companyService;

    @Mock
    private BranchMapper branchMapper;

    @InjectMocks
    private BranchServiceImpl branchService;

    private CreateBranchRequest createRequest;
    private UpdateBranchRequest updateRequest;
    private BranchResponse branchResponse;
    private Branch branch;
    private Company company;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setup() {
        userPrincipal = UserPrincipal.builder()
                .id(1L)
                .companyId(10L)
                .email("admin@humify.com")
                .build();

        company = Company.builder()
                .name("Humify Copration")
                .build();

        createRequest = new CreateBranchRequest();

        updateRequest = new UpdateBranchRequest();
        updateRequest.setName("CN Phú Quốc");
        updateRequest.setField("Du lịch - Khách sạn");
        updateRequest.setStatus("PENDING");

        branch = Branch.builder()
                .branchCode("BR3921")
                .name("VP Quận 2")
                .field("Agriculture")
                .status(BranchStatus.ACTIVE)
                .company(company)
                .build();

        branchResponse = BranchResponse.builder()
                .id(100L)
                .branchCode("BR3921")
                .name("VP Quận 2")
                .build();
    }

    // BR-SRV-01: createBranch - Thành công
    @Test
    void createBranch_success() {
        when(companyService.getCompanyById(10L)).thenReturn(company);
        when(branchMapper.toBranch(createRequest)).thenReturn(branch);
        when(branchRepository.save(any(Branch.class))).thenReturn(branch);
        when(branchMapper.toBranchResponse(branch)).thenReturn(branchResponse);

        BranchResponse result = branchService.createBranch(userPrincipal, createRequest);

        assertNotNull(result);
        assertThat(result.getId()).isEqualTo(100L);
        verify(branchRepository).save(any(Branch.class));
    }

    // BR-SRV-02: createBranch - Thất bại khi không có thông tin Công ty
    @Test
    void createBranch_companyNotFound() {
        when(companyService.getCompanyById(10L))
                .thenThrow(new AppException(ErrorCode.COMPANY_NOT_FOUND));

        AppException exception = assertThrows(
                AppException.class,
                () -> branchService.createBranch(userPrincipal, createRequest)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COMPANY_NOT_FOUND);
        verify(branchRepository, never()).save(any());
    }

    // BR-SRV-03: getBranchById - Thành công
    @Test
    void getBranchById_success() {
        when(branchRepository.findById(100L)).thenReturn(Optional.of(branch));

        Branch result = branchService.getBranchById(100L);

        assertNotNull(result);
        assertThat(result.getName()).isEqualTo(branch.getName());
    }

    // BR-SRV-04: getBranchById - Thất bại do ID không tồn tại
    @Test
    void getBranchById_notFound() {
        when(branchRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> branchService.getBranchById(999L)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BRANCH_NOT_FOUND);
    }

    // BR-SRV-05: getBranchResponseById - Thành công
    @Test
    void getBranchResponseById_success() {
        when(branchRepository.findById(100L)).thenReturn(Optional.of(branch));
        when(branchMapper.toBranchResponse(branch)).thenReturn(branchResponse);

        BranchResponse result = branchService.getBranchResponseById(100L);

        assertNotNull(result);
        assertThat(result.getId()).isEqualTo(100L);
    }

    // BR-SRV-06: getBranchResponseById - Thất bại do ID không tồn tại
    @Test
    void getBranchResponseById_notFound() {
        when(branchRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> branchService.getBranchResponseById(999L)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BRANCH_NOT_FOUND);
    }

    // BR-SRV-07: getBranchesByCompanyId - Thành công
    @Test
    void getBranchesByCompanyId_success() {
        when(branchRepository.findByCompanyId(10L)).thenReturn(List.of(branch));
        when(branchMapper.toBranchResponseList(anyList())).thenReturn(List.of(branchResponse));

        List<BranchResponse> result = branchService.getBranchesByCompanyId(10L);

        assertNotNull(result);
        assertThat(result).isNotEmpty();
    }

    // BR-SRV-08: updateBranch - Thành công
    @Test
    void updateBranch_success() {
        when(branchRepository.findById(100L)).thenReturn(Optional.of(branch));
        when(branchRepository.save(branch)).thenReturn(branch);
        when(branchMapper.toBranchResponse(branch)).thenReturn(branchResponse);

        BranchResponse result = branchService.updateBranch(100L, updateRequest);

        assertNotNull(result);
        assertThat(branch.getStatus()).isEqualTo(BranchStatus.PENDING);
        verify(branchRepository).save(branch);
    }

    // BR-SRV-09: deleteBranch - Thành công
    @Test
    void deleteBranch_success() {
        when(branchRepository.findById(100L)).thenReturn(Optional.of(branch));
        when(branchRepository.save(branch)).thenReturn(branch);

        assertDoesNotThrow(() -> branchService.deleteBranch(100L));

        assertThat(branch.getStatus()).isEqualTo(BranchStatus.CLOSED);

        verify(branchRepository).save(branch);
        verify(branchRepository, never()).delete(any(Branch.class));
    }

    // BR-SRV-10: existsByIdAndCompanyId - Thành công
    @Test
    void existsByIdAndCompanyId_success() {
        when(branchRepository.existsByIdAndCompanyId(100L, 10L)).thenReturn(true);

        boolean result = branchService.existsByIdAndCompanyId(100L, 10L);

        assertTrue(result);
    }

    @Test
    @DisplayName("Update branch when branch not found")
    void updateBranch_branchNotFound() {
        when(branchRepository.findById(100L)).thenReturn(Optional.empty());

        final AppException exception = assertThrows(
                AppException.class,
                () -> branchService.updateBranch(100L, updateRequest)
        );

        verify(branchRepository, times(1)).findById(100L);
        assertEquals(ErrorCode.BRANCH_NOT_FOUND, exception.getErrorCode());
        verify(branchRepository, never()).save(any(Branch.class));
        verify(branchMapper, never()).toBranchResponse(any());
    }

    // BR-SRV-11: getAllBranches - Thành công, có dữ liệu
    @Test
    @DisplayName("Get all branches - success with data")
    void getAllBranches_success() {
        Page<Branch> branchPage = new PageImpl<>(List.of(branch), PageRequest.of(0, 10), 1);

        when(branchRepository.findAll(any(Pageable.class)))
                .thenReturn(branchPage);
        when(branchMapper.toBranchResponse(branch)).thenReturn(branchResponse);

        PageResponse<BranchResponse> result = branchService.getAllBranches(1, 10, "name:asc");

        assertNotNull(result);
        assertThat(result.getPageNo()).isEqualTo(1);
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getId()).isEqualTo(100L);
        verify(branchRepository).findAll(any(Pageable.class));
    }

    // BR-SRV-12: getAllBranches - Thành công, không có dữ liệu
    @Test
    @DisplayName("Get all branches - success with empty result")
    void getAllBranches_emptyResult() {
        Page<Branch> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(branchRepository.findAll(any(Pageable.class)))
                .thenReturn(emptyPage);

        PageResponse<BranchResponse> result = branchService.getAllBranches(1, 10);

        assertNotNull(result);
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        verify(branchMapper, never()).toBranchResponse(any());
    }

    // BR-SRV-13: getReferenceById - Thành công
    @Test
    @DisplayName("Get reference by id - success")
    void getReferenceById_success() {
        when(branchRepository.getReferenceById(100L)).thenReturn(branch);

        Branch result = branchService.getReferenceById(100L);

        assertNotNull(result);
        assertThat(result).isEqualTo(branch);
        verify(branchRepository).getReferenceById(100L);
    }

    // BR-SRV-14: getById - Thành công
    @Test
    @DisplayName("Get by id - success")
    void getById_success() {
        when(branchRepository.findById(100L)).thenReturn(Optional.of(branch));

        Branch result = branchService.getById(100L);

        assertNotNull(result);
        assertThat(result).isEqualTo(branch);
    }

    // BR-SRV-15: getById - Thất bại do không tồn tại
    @Test
    @DisplayName("Get by id - not found")
    void getById_notFound() {
        when(branchRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> branchService.getById(999L)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BRANCH_NOT_FOUND);
    }

    // BR-SRV-16: existsById - Thành công, tồn tại
    @Test
    @DisplayName("Exists by id - true")
    void existsById_true() {
        when(branchRepository.existsById(100L)).thenReturn(true);

        boolean result = branchService.existsById(100L);

        assertTrue(result);
    }

    // BR-SRV-17: existsById - Thành công, không tồn tại
    @Test
    @DisplayName("Exists by id - false")
    void existsById_false() {
        when(branchRepository.existsById(999L)).thenReturn(false);

        boolean result = branchService.existsById(999L);

        assertFalse(result);
    }

    // BR-SRV-18: existsByIdAndCompanyId - Thất bại (không tồn tại)
    @Test
    @DisplayName("Exists by id and company id - false")
    void existsByIdAndCompanyId_false() {
        when(branchRepository.existsByIdAndCompanyId(999L, 10L)).thenReturn(false);

        boolean result = branchService.existsByIdAndCompanyId(999L, 10L);

        assertFalse(result);
    }

    // BR-SRV-19: findByName - Thành công, tìm thấy
    @Test
    @DisplayName("Find by name - found")
    void findByName_found() {
        when(branchRepository.findByName("VP Quận 2")).thenReturn(Optional.of(branch));

        Optional<Branch> result = branchService.findByName("VP Quận 2");

        assertTrue(result.isPresent());
        assertThat(result.get().getName()).isEqualTo("VP Quận 2");
    }

    // BR-SRV-20: findByName - Không tìm thấy
    @Test
    @DisplayName("Find by name - not found")
    void findByName_notFound() {
        when(branchRepository.findByName("Không tồn tại")).thenReturn(Optional.empty());

        Optional<Branch> result = branchService.findByName("Không tồn tại");

        assertTrue(result.isEmpty());
    }

    // BR-SRV-21: save - Thành công
    @Test
    @DisplayName("Save branch - success")
    void save_success() {
        when(branchRepository.save(branch)).thenReturn(branch);

        Branch result = branchService.save(branch);

        assertNotNull(result);
        assertThat(result).isEqualTo(branch);
        verify(branchRepository).save(branch);
    }

}