package com.lamdayne.humify.position.service.impl;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PositionServiceImplTest {

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

    // POS-SRV-01: createPosition - Thành công
    @Test
    void createPosition_success() {
        when(companyService.getCompanyById(10L)).thenReturn(company);
        when(positionMapper.toPosition(createRequest)).thenReturn(position);
        when(positionRepository.save(any(Position.class))).thenReturn(position);
        when(positionMapper.toPositionResponse(position)).thenReturn(positionResponse);

        PositionResponse result = positionService.createPosition(userPrincipal, createRequest);

        assertNotNull(result);
        assertThat(result.getId()).isEqualTo(100L);
        verify(positionRepository).save(any(Position.class));
    }

    // POS-SRV-02: createPosition - Thất bại do Phòng ban không tồn tại
    @Test
    void createPosition_departmentNotFound() {
        when(companyService.getCompanyById(10L))
                .thenThrow(new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));

        AppException exception = assertThrows(
                AppException.class,
                () -> positionService.createPosition(userPrincipal, createRequest)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DEPARTMENT_NOT_FOUND);
        verify(positionRepository, never()).save(any());
    }

    // POS-SRV-03: getPositionById - Thành công
    @Test
    void getPositionById_success() {
        when(positionRepository.findById(100L)).thenReturn(Optional.of(position));

        Position result = positionService.getPositionById(100L);

        assertNotNull(result);
        assertThat(result.getName()).isEqualTo(position.getName());
    }

    // POS-SRV-04: getPositionById - Thất bại do ID không tồn tại
    @Test
    void getPositionById_notFound() {
        when(positionRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> positionService.getPositionById(999L)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.POSITION_NOT_FOUND);
    }

    // POS-SRV-05: getPositionResponseById - Thành công
    @Test
    void getPositionResponseById_success() {
        when(positionRepository.findById(100L)).thenReturn(Optional.of(position));
        when(positionMapper.toPositionResponse(position)).thenReturn(positionResponse);

        PositionResponse result = positionService.getPositionResponseById(100L);

        assertNotNull(result);
        assertThat(result.getId()).isEqualTo(100L);
    }

    // POS-SRV-06: getPositionResponseById - Thất bại do ID không tồn tại
    @Test
    void getPositionResponseById_notFound() {
        when(positionRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> positionService.getPositionResponseById(999L)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.POSITION_NOT_FOUND);
    }

    // POS-SRV-07: getPositionsByDepartmentId - Thành công
    @Test
    void getPositionsByDepartmentId_success() {
        when(positionRepository.findAll()).thenReturn(List.of(position));

        List<Position> result = positionService.getPositionsByDepartmentId(1L);

        assertNotNull(result);
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getName()).isEqualTo(position.getName());
    }

    // POS-SRV-08: updatePosition - Thành công
    @Test
    void updatePosition_success() {
        when(positionRepository.findById(100L)).thenReturn(Optional.of(position));
        when(positionRepository.save(position)).thenReturn(position);
        when(positionMapper.toPositionResponse(position)).thenReturn(positionResponse);

        PositionResponse result = positionService.updatePosition(100L, createRequest);

        assertNotNull(result);
        verify(positionRepository).save(position);
    }

    // POS-SRV-09: deletePosition - Thành công
    @Test
    void deletePosition_success() {
        when(positionRepository.existsById(100L)).thenReturn(true);
        doNothing().when(positionRepository).deleteById(100L);

        assertDoesNotThrow(() -> positionService.deletePosition(100L));

        verify(positionRepository).deleteById(100L);
    }

    // POS-SRV-10: existsById - Thành công
    @Test
    void existsById_success() {
        when(positionRepository.existsById(100L)).thenReturn(true);

        boolean result = positionService.existsById(100L);

        assertTrue(result);
        verify(positionRepository).existsById(100L);
    }
}
