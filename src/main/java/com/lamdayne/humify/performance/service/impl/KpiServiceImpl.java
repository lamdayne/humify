package com.lamdayne.humify.performance.service.impl;

import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyAccessService;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
import com.lamdayne.humify.performance.dto.request.CreateKpiRequest;
import com.lamdayne.humify.performance.dto.request.UpdateKpiProgressRequest;
import com.lamdayne.humify.performance.dto.response.KpiResponse;
import com.lamdayne.humify.performance.entity.Kpi;
import com.lamdayne.humify.performance.enums.KpiStatus;
import com.lamdayne.humify.performance.mapper.KpiMapper;
import com.lamdayne.humify.performance.repository.KpiRepository;
import com.lamdayne.humify.performance.service.KpiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiServiceImpl implements KpiService {

    private final KpiRepository kpiRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyAccessService companyAccessService;
    private final KpiMapper kpiMapper;

    @Override
    @Transactional
    public KpiResponse createKpi(Long employeeId, CreateKpiRequest request) {
        Long companyId = CompanyContext.getCompanyId();

        Employee employee = employeeRepository.findById(employeeId)
                .filter(emp -> emp.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        Double currentTotalWeight = kpiRepository.sumWeightByEmployeeIdAndCompanyId(employeeId, companyId, null);
        if (currentTotalWeight + request.getWeight() > 1.0) {
            throw new AppException(ErrorCode.KPI_WEIGHT_INVALID);
        }

        Company company = companyAccessService.getReferenceById(companyId);

        Kpi kpi = Kpi.builder()
                .company(company)
                .employee(employee)
                .title(request.getTitle())
                .description(request.getDescription())
                .targetValue(request.getTargetValue())
                .unit(request.getUnit())
                .weight(request.getWeight())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        return kpiMapper.toResponse(kpiRepository.save(kpi));
    }

    @Override
    public List<KpiResponse> getKpisByEmployee(Long employeeId, LocalDate startDate, LocalDate endDate) {
        Long companyId = CompanyContext.getCompanyId();
        return kpiRepository.findAllByEmployeeIdAndDateRange(employeeId, companyId, startDate, endDate)
                .stream()
                .map(kpiMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public KpiResponse updateKpi(Long id, CreateKpiRequest request) {
        Long companyId = CompanyContext.getCompanyId();
        Kpi kpi = kpiRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(ErrorCode.KPI_NOT_FOUND));

        Double currentTotalWeight = kpiRepository.sumWeightByEmployeeIdAndCompanyId(kpi.getEmployee().getId(), companyId, kpi.getId());
        if (currentTotalWeight + request.getWeight() > 1.0) {
            throw new AppException(ErrorCode.KPI_WEIGHT_INVALID);
        }

        kpi.setTitle(request.getTitle());
        kpi.setDescription(request.getDescription());
        kpi.setTargetValue(request.getTargetValue());
        kpi.setUnit(request.getUnit());
        kpi.setWeight(request.getWeight());
        kpi.setStartDate(request.getStartDate());
        kpi.setEndDate(request.getEndDate());

        return kpiMapper.toResponse(kpiRepository.save(kpi));
    }

    @Override
    @Transactional
    public KpiResponse updateProgress(Long id, UpdateKpiProgressRequest request) {
        Long companyId = CompanyContext.getCompanyId();
        Kpi kpi = kpiRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(ErrorCode.KPI_NOT_FOUND));

        kpi.setCurrentValue(request.getCurrentValue());

        if (kpi.getCurrentValue() >= kpi.getTargetValue()) {
            kpi.setStatus(KpiStatus.ACHIEVED);
        } else {
            kpi.setStatus(KpiStatus.IN_PROGRESS);
        }

        return kpiMapper.toResponse(kpiRepository.save(kpi));
    }

    @Override
    @Transactional
    public void deleteKpi(Long id) {
        Long companyId = CompanyContext.getCompanyId();
        Kpi kpi = kpiRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(ErrorCode.KPI_NOT_FOUND));
        kpiRepository.delete(kpi);
    }
}