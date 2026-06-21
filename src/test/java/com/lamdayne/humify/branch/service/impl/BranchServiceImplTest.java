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
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}