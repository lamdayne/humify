package com.lamdayne.humify.position.service.impl;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyService;
import com.lamdayne.humify.position.dto.request.CreatePositionRequest;
import com.lamdayne.humify.position.dto.response.PositionResponse;
import com.lamdayne.humify.position.entity.Position;
import com.lamdayne.humify.position.mapper.PositionMapper;
import com.lamdayne.humify.position.repository.PositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PositionServiceImplTest {

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private CompanyService companyService;

    @Mock
    private PositionMapper positionMapper;

    @InjectMocks
    private PositionServiceImpl positionService;

    private CreatePositionRequest createRequest;
    private PositionResponse positionResponse;
    private Position position;
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
                .name("Humify Corp")
                .build();
        company.setId(10L);

        createRequest = new CreatePositionRequest();
        createRequest.setName("Senior Frontend Developer");
        createRequest.setDescription("Responsible for building user interfaces");

        position = Position.builder()
                .name("Senior Frontend Developer")
                .description("Responsible for building user interfaces")
                .company(company)
                .build();

        positionResponse = PositionResponse.builder()
                .id(100L)
                .name("Senior Frontend Developer")
                .description("Responsible for building user interfaces")
                .build();
    }

    // ============================================================
    // POS-SRV-01: createPosition - Thành công
    // ============================================================
    @Test
    void createPosition_success() {
        when(companyService.getCompanyById(10L)).thenReturn(company);
        when(positionMapper.toPosition(createRequest)).thenReturn(position);
        when(positionRepository.save(any(Position.class))).thenReturn(position);
        when(positionMapper.toPositionResponse(position)).thenReturn(positionResponse);

        PositionResponse result = positionService.createPosition(userPrincipal, createRequest);

        assertNotNull(result);
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getName()).isEqualTo("Senior Frontend Developer");
        verify(companyService).getCompanyById(10L);
        verify(positionMapper).toPosition(createRequest);
        verify(positionRepository).save(any(Position.class));
        verify(positionMapper).toPositionResponse(position);
    }

    // ============================================================
    // POS-SRV-02: createPosition - Thất bại do Company không tồn tại
    // ============================================================
    @Test
    void createPosition_companyNotFound_throwsException() {
        when(companyService.getCompanyById(10L))
                .thenThrow(new AppException(ErrorCode.COMPANY_NOT_FOUND));

        AppException exception = assertThrows(
                AppException.class,
                () -> positionService.createPosition(userPrincipal, createRequest)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COMPANY_NOT_FOUND);
        verify(positionRepository, never()).save(any());
    }

    // ============================================================
    // POS-SRV-03: getAllPositions - Thành công có dữ liệu và sort
    // ============================================================
    @Test
    void getAllPositions_withSort_success() {
        Page<Position> page = new PageImpl<>(List.of(position));
        when(positionRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(positionMapper.toPositionResponse(position)).thenReturn(positionResponse);

        PageResponse<PositionResponse> result = positionService.getAllPositions(1, 10, "name:asc");

        assertNotNull(result);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getName()).isEqualTo("Senior Frontend Developer");
        assertThat(result.getPageNo()).isEqualTo(1);
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(1L);
        verify(positionRepository).findAll(any(Pageable.class));
    }

    // ============================================================
    // POS-SRV-04: getAllPositions - Trang rỗng, không có sort
    // ============================================================
    @Test
    void getAllPositions_noSort_emptyPage() {
        Page<Position> emptyPage = new PageImpl<>(List.of());
        when(positionRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        PageResponse<PositionResponse> result = positionService.getAllPositions(1, 5);

        assertNotNull(result);
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getPageSize()).isEqualTo(5);
    }

    // ============================================================
    // POS-SRV-05: getReferenceById - Thành công
    // ============================================================
    @Test
    void getReferenceById_success() {
        when(positionRepository.getReferenceById(100L)).thenReturn(position);

        Position result = positionService.getReferenceById(100L);

        assertNotNull(result);
        assertThat(result.getName()).isEqualTo("Senior Frontend Developer");
        verify(positionRepository).getReferenceById(100L);
    }

    // ============================================================
    // POS-SRV-06: getById - Thành công
    // ============================================================
    @Test
    void getById_success() {
        when(positionRepository.findById(100L)).thenReturn(Optional.of(position));

        Position result = positionService.getById(100L);

        assertNotNull(result);
        assertThat(result.getName()).isEqualTo("Senior Frontend Developer");
        verify(positionRepository).findById(100L);
    }

    // ============================================================
    // POS-SRV-07: getById - Thất bại do không tìm thấy
    // ============================================================
    @Test
    void getById_notFound_throwsException() {
        when(positionRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> positionService.getById(999L)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.POSITION_NOT_FOUND);
    }

    // ============================================================
    // POS-SRV-08: existsById - Trả về true
    // ============================================================
    @Test
    void existsById_returnsTrue() {
        when(positionRepository.existsById(100L)).thenReturn(true);

        boolean result = positionService.existsById(100L);

        assertTrue(result);
        verify(positionRepository).existsById(100L);
    }

    // ============================================================
    // POS-SRV-09: existsById - Trả về false
    // ============================================================
    @Test
    void existsById_returnsFalse() {
        when(positionRepository.existsById(999L)).thenReturn(false);

        boolean result = positionService.existsById(999L);

        assertFalse(result);
        verify(positionRepository).existsById(999L);
    }

    // ============================================================
    // POS-SRV-10: existsByIdAndCompanyId - Trả về true
    // ============================================================
    @Test
    void existsByIdAndCompanyId_returnsTrue() {
        when(positionRepository.existsByIdAndCompanyId(100L, 10L)).thenReturn(true);

        boolean result = positionService.existsByIdAndCompanyId(100L, 10L);

        assertTrue(result);
        verify(positionRepository).existsByIdAndCompanyId(100L, 10L);
    }

    // ============================================================
    // POS-SRV-11: existsByIdAndCompanyId - Trả về false
    // ============================================================
    @Test
    void existsByIdAndCompanyId_returnsFalse() {
        when(positionRepository.existsByIdAndCompanyId(999L, 10L)).thenReturn(false);

        boolean result = positionService.existsByIdAndCompanyId(999L, 10L);

        assertFalse(result);
        verify(positionRepository).existsByIdAndCompanyId(999L, 10L);
    }

    // ============================================================
    // POS-SRV-12: findByName - Tìm thấy
    // ============================================================
    @Test
    void findByName_found_returnsOptional() {
        when(positionRepository.findByName("Senior Frontend Developer"))
                .thenReturn(Optional.of(position));

        Optional<Position> result = positionService.findByName("Senior Frontend Developer");

        assertTrue(result.isPresent());
        assertThat(result.get().getName()).isEqualTo("Senior Frontend Developer");
        verify(positionRepository).findByName("Senior Frontend Developer");
    }

    // ============================================================
    // POS-SRV-13: findByName - Không tìm thấy
    // ============================================================
    @Test
    void findByName_notFound_returnsEmpty() {
        when(positionRepository.findByName("Unknown")).thenReturn(Optional.empty());

        Optional<Position> result = positionService.findByName("Unknown");

        assertFalse(result.isPresent());
        verify(positionRepository).findByName("Unknown");
    }

    // ============================================================
    // POS-SRV-14: findAll - Có dữ liệu
    // ============================================================
    @Test
    void findAll_returnsList() {
        when(positionRepository.findAll()).thenReturn(List.of(position));

        List<Position> result = positionService.findAll();

        assertNotNull(result);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Senior Frontend Developer");
        verify(positionRepository).findAll();
    }

    // ============================================================
    // POS-SRV-15: findAll - Danh sách rỗng
    // ============================================================
    @Test
    void findAll_returnsEmptyList() {
        when(positionRepository.findAll()).thenReturn(List.of());

        List<Position> result = positionService.findAll();

        assertNotNull(result);
        assertThat(result).isEmpty();
        verify(positionRepository).findAll();
    }

    // ============================================================
    // POS-SRV-16: updatePosition - Thành công
    // ============================================================
    @Test
    void updatePosition_success() {
        when(positionRepository.findById(100L)).thenReturn(Optional.of(position));
        when(positionRepository.save(position)).thenReturn(position);
        when(positionMapper.toPositionResponse(position)).thenReturn(positionResponse);

        PositionResponse result = positionService.updatePosition(100L, createRequest);

        assertNotNull(result);
        assertThat(result.getId()).isEqualTo(100L);
        verify(positionMapper).updatePosition(position, createRequest);
        verify(positionRepository).save(position);
        verify(positionMapper).toPositionResponse(position);
    }

    // ============================================================
    // POS-SRV-17: updatePosition - Thất bại do ID không tồn tại
    // ============================================================
    @Test
    void updatePosition_notFound_throwsException() {
        when(positionRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> positionService.updatePosition(999L, createRequest)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.POSITION_NOT_FOUND);
        verify(positionRepository, never()).save(any());
    }

    // ============================================================
    // POS-SRV-18: deletePosition - Thành công
    // ============================================================
    @Test
    void deletePosition_success() {
        when(positionRepository.existsById(100L)).thenReturn(true);
        doNothing().when(positionRepository).deleteById(100L);

        assertDoesNotThrow(() -> positionService.deletePosition(100L));

        verify(positionRepository).existsById(100L);
        verify(positionRepository).deleteById(100L);
    }

    // ============================================================
    // POS-SRV-19: deletePosition - Thất bại do ID không tồn tại
    // ============================================================
    @Test
    void deletePosition_notFound_throwsException() {
        when(positionRepository.existsById(999L)).thenReturn(false);

        AppException exception = assertThrows(
                AppException.class,
                () -> positionService.deletePosition(999L)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.POSITION_NOT_FOUND);
        verify(positionRepository, never()).deleteById(any());
    }

    // ============================================================
    // POS-SRV-20: getPositionById - Thành công
    // ============================================================
    @Test
    void getPositionById_success() {
        when(positionRepository.findById(100L)).thenReturn(Optional.of(position));

        Position result = positionService.getPositionById(100L);

        assertNotNull(result);
        assertThat(result.getName()).isEqualTo(position.getName());
        verify(positionRepository).findById(100L);
    }

    // ============================================================
    // POS-SRV-21: getPositionById - Thất bại do ID không tồn tại
    // ============================================================
    @Test
    void getPositionById_notFound() {
        when(positionRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> positionService.getPositionById(999L)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.POSITION_NOT_FOUND);
    }

    // ============================================================
    // POS-SRV-22: getPositionResponseById - Thành công
    // ============================================================
    @Test
    void getPositionResponseById_success() {
        when(positionRepository.findById(100L)).thenReturn(Optional.of(position));
        when(positionMapper.toPositionResponse(position)).thenReturn(positionResponse);

        PositionResponse result = positionService.getPositionResponseById(100L);

        assertNotNull(result);
        assertThat(result.getId()).isEqualTo(100L);
        verify(positionMapper).toPositionResponse(position);
    }

    // ============================================================
    // POS-SRV-23: getPositionResponseById - Thất bại do ID không tồn tại
    // ============================================================
    @Test
    void getPositionResponseById_notFound() {
        when(positionRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> positionService.getPositionResponseById(999L)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.POSITION_NOT_FOUND);
    }

    // ============================================================
    // POS-SRV-24: getPositionsByDepartmentId - Thành công có dữ liệu
    // ============================================================
    @Test
    void getPositionsByDepartmentId_success() {
        when(positionRepository.findAll()).thenReturn(List.of(position));

        List<Position> result = positionService.getPositionsByDepartmentId(1L);

        assertNotNull(result);
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getName()).isEqualTo(position.getName());
        verify(positionRepository).findAll();
    }

    // ============================================================
    // POS-SRV-25: getPositionsByDepartmentId - Danh sách rỗng
    // ============================================================
    @Test
    void getPositionsByDepartmentId_empty() {
        when(positionRepository.findAll()).thenReturn(List.of());

        List<Position> result = positionService.getPositionsByDepartmentId(99L);

        assertNotNull(result);
        assertThat(result).isEmpty();
    }

    // ============================================================
    // POS-SRV-26: save(Position) - Thành công
    // ============================================================
    @Test
    void save_position_success() {
        when(positionRepository.save(position)).thenReturn(position);

        Position result = positionService.save(position);

        assertNotNull(result);
        assertThat(result.getName()).isEqualTo("Senior Frontend Developer");
        verify(positionRepository).save(position);
    }
}
