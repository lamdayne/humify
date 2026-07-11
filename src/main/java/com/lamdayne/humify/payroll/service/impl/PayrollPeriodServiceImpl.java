package com.lamdayne.humify.payroll.service.impl;

import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyService;
import com.lamdayne.humify.payroll.dto.request.CreatePayrollPeriodRequest;
import com.lamdayne.humify.payroll.dto.response.PayrollPeriodResponse;
import com.lamdayne.humify.payroll.entity.PayrollPeriod;
import com.lamdayne.humify.payroll.enums.PayrollPeriodStatus;
import com.lamdayne.humify.payroll.mapper.PayrollPeriodMapper;
import com.lamdayne.humify.payroll.repository.PayrollPeriodRepository;
import com.lamdayne.humify.payroll.service.PayrollPeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PayrollPeriodServiceImpl implements PayrollPeriodService {

    private final PayrollPeriodRepository payrollPeriodRepository;
    private final CompanyService companyService;
    private final PayrollPeriodMapper payrollPeriodMapper;

    @Override
    @Transactional
    public PayrollPeriodResponse createPayrollPeriod(Long companyId, CreatePayrollPeriodRequest request) {
        Company company = companyService.getCompanyById(companyId);

        if (payrollPeriodRepository.existsByCompanyIdAndMonthAndYear(companyId, request.getMonth(), request.getYear())) {
            throw new AppException(ErrorCode.PAYROLL_PERIOD_EXISTED);
        }

        PayrollPeriod payrollPeriod = PayrollPeriod.builder()
                .company(company)
                .name(request.getName())
                .month(request.getMonth())
                .year(request.getYear())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .standardWorkDays(request.getStandardWorkDays())
                .status(PayrollPeriodStatus.DRAFT)
                .build();

        return payrollPeriodMapper.toResponse(payrollPeriodRepository.save(payrollPeriod));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PayrollPeriodResponse> getPayrollPeriods(Long companyId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<PayrollPeriod> pageData = payrollPeriodRepository.findByCompanyIdOrderByYearDescMonthDesc(companyId, pageable);

        List<PayrollPeriodResponse> content = pageData.getContent().stream()
                .map(payrollPeriodMapper::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<PayrollPeriodResponse>builder()
                .pageNo(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .items(content)
                .build();
    }

    @Override
    @Transactional
    public int calculatePayroll(Long id, Long companyId) {
        PayrollPeriod period = payrollPeriodRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYROLL_PERIOD_NOT_FOUND));

        // Kiểm tra trạng thái kỳ lương
        if (period.getStatus() != PayrollPeriodStatus.DRAFT) {
            throw new AppException(ErrorCode.PAYROLL_PERIOD_LOCKED);
        }

        /* =====================================================================
           TODO: THUẬT TOÁN TÍNH LƯƠNG SẼ ĐƯỢC THÊM VÀO ĐÂY SAU KHI CÓ CÁC MODULE KHÁC
           1. Lấy danh sách nhân viên có hợp đồng ACTIVE trong khoảng startDate -> endDate
           2. Vòng lặp tính lương cho từng nhân viên:
              a. Quét bảng attendances lấy actual_work_days
              b. Quét bảng leave_requests lấy paid_leave_days và unpaid_leave_days
              c. Tính Base Salary: (Lương cơ bản / standardWorkDays) * (actual + paid_leave)
              d. Cộng phụ cấp (Allowances)
              e. Trừ BHXH (Social Insurance)
              f. Tính Thuế TNCN (PIT) lũy tiến
              g. Lưu vào bảng Payslips (Nếu đã có thì update, chưa có thì insert)
           ===================================================================== */

        // Mock data: Giả sử tính toán thành công cho 125 nhân viên
        int calculatedEmployeesCount = 125;

        return calculatedEmployeesCount;
    }
}
