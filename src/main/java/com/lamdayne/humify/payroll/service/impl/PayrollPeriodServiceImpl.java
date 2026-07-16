package com.lamdayne.humify.payroll.service.impl;

import com.lamdayne.humify.attendance.entity.Attendance;
import com.lamdayne.humify.attendance.repository.AttendanceRepository;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyService;
import com.lamdayne.humify.employee.entity.EmployeeContract;
import com.lamdayne.humify.employee.enums.ContractStatus;
import com.lamdayne.humify.employee.repository.EmployeeContractRepository;
import com.lamdayne.humify.payroll.dto.request.CreatePayrollPeriodRequest;
import com.lamdayne.humify.payroll.dto.response.PayrollPeriodResponse;
import com.lamdayne.humify.payroll.entity.PayrollPeriod;
import com.lamdayne.humify.payroll.entity.Payslip;
import com.lamdayne.humify.payroll.enums.PayrollPeriodStatus;
import com.lamdayne.humify.payroll.enums.PayslipStatus;
import com.lamdayne.humify.payroll.mapper.PayrollPeriodMapper;
import com.lamdayne.humify.payroll.repository.PayrollPeriodRepository;
import com.lamdayne.humify.payroll.repository.PayslipRepository;
import com.lamdayne.humify.payroll.service.PayrollPeriodService;
import com.lamdayne.humify.payroll.util.PayrollTaxCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PayrollPeriodServiceImpl implements PayrollPeriodService {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final EmployeeContractRepository employeeContractRepository;
    private final AttendanceRepository attendanceRepository;
    private final PayslipRepository payslipRepository;

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
    public int calculate(Long payrollPeriodId) {
        PayrollPeriod period = payrollPeriodRepository.findById(payrollPeriodId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYROLL_PERIOD_NOT_FOUND));

        if (period.getStatus() != PayrollPeriodStatus.DRAFT) {
            throw new AppException(ErrorCode.PAYROLL_PERIOD_LOCKED);
        }

        List<EmployeeContract> activeContracts = employeeContractRepository
                .findActiveContractsOverlappingPeriod(ContractStatus.ACTIVE, period.getStartDate(), period.getEndDate());

        int processed = 0;
        for (EmployeeContract contract : activeContracts) {
            calculateForEmployee(period, contract);
            processed++;
        }
        return processed;
    }

    private void calculateForEmployee(PayrollPeriod period, EmployeeContract contract) {
        // 1. Tổng hợp công chấm công trong khoảng [start_date, end_date] của kỳ lương
        //    TODO: kiểm tra lại đúng tên các giá trị trong AttendanceStatus của bạn — mình đang
        //    giả định PRESENT, LATE, REMOTE (1 công), HALF_DAY (0.5 công), LEAVE (nghỉ có lương),
        //    ABSENT (nghỉ không lương), khớp với mục 4.2 tài liệu thiết kế.
        List<Attendance> attendances = attendanceRepository.findByEmployeeIdAndWorkDateBetween(
                 contract.getEmployee().getId(), period.getStartDate(), period.getEndDate()
        );

        BigDecimal actualWorkDays = BigDecimal.ZERO;
        BigDecimal paidLeaveDays = BigDecimal.ZERO;
        BigDecimal unpaidLeaveDays = BigDecimal.ZERO;

        for (Attendance a : attendances) {
            switch (a.getStatus()) {
                case PRESENT, LATE, REMOTE -> actualWorkDays = actualWorkDays.add(BigDecimal.ONE);
                case HALF_DAY -> actualWorkDays = actualWorkDays.add(new BigDecimal("0.5"));
                case LEAVE -> paidLeaveDays = paidLeaveDays.add(BigDecimal.ONE);
                case ABSENT -> unpaidLeaveDays = unpaidLeaveDays.add(BigDecimal.ONE);
            }
        }

        // 2. Lương theo ngày công = base_salary * (actual_work_days + paid_leave_days) / standard_work_days
        BigDecimal standardWorkDays = period.getStandardWorkDays();
        BigDecimal salaryByWorkDays = contract.getBaseSalary()
                .multiply(actualWorkDays.add(paidLeaveDays))
                .divide(standardWorkDays, SCALE, ROUNDING);

        // 3. Phụ cấp cố định từ hợp đồng
        BigDecimal totalAllowances = nz(contract.getAllowanceLunch())
                .add(nz(contract.getAllowancePhone()))
                .add(nz(contract.getAllowanceTransport()))
                .add(nz(contract.getAllowanceOther()));

        // 4. Thưởng KPI / dự án / OT: hệ thống chưa có module đánh giá hiệu suất (KPI) hay bảng công
        //    tăng ca riêng biệt -> mặc định 0, HR bổ sung thủ công qua PUT /payslips/{id} (otherBonuses)
        //    hoặc tích hợp thêm khi có PerformanceReviewService/OvertimeService.
        BigDecimal bonusKpi = BigDecimal.ZERO;
        BigDecimal bonusProject = BigDecimal.ZERO;
        BigDecimal otHours = BigDecimal.ZERO;
        BigDecimal otSalary = BigDecimal.ZERO;
        BigDecimal otherBonuses = BigDecimal.ZERO;

        // 5. Tổng thu nhập trước bảo hiểm & thuế
        BigDecimal grossSalary = salaryByWorkDays
                .add(totalAllowances)
                .add(bonusKpi)
                .add(bonusProject)
                .add(otherBonuses)
                .add(otSalary);

        // 6. Bảo hiểm bắt buộc trừ vào lương (tính trên insurance_salary của hợp đồng)
        BigDecimal insuranceSalary = contract.getInsuranceSalary();
        BigDecimal socialInsurance = PayrollTaxCalculator.calculateSocialInsurance(insuranceSalary);
        BigDecimal healthInsurance = PayrollTaxCalculator.calculateHealthInsurance(insuranceSalary);
        BigDecimal unemploymentInsurance = PayrollTaxCalculator.calculateUnemploymentInsurance(insuranceSalary);
        BigDecimal totalCompulsoryInsurance = socialInsurance.add(healthInsurance).add(unemploymentInsurance);

        // 7. Thuế TNCN lũy tiến từng phần
        int dependents = contract.getTaxableDependents() == null ? 0 : contract.getTaxableDependents();
        BigDecimal taxableIncome = PayrollTaxCalculator.calculateTaxableIncome(grossSalary, dependents, totalCompulsoryInsurance);
        BigDecimal personalIncomeTax = PayrollTaxCalculator.calculatePersonalIncomeTax(taxableIncome);

        BigDecimal otherDeductions = BigDecimal.ZERO;

        // 8. Thực nhận
        BigDecimal netSalary = grossSalary
                .subtract(socialInsurance)
                .subtract(healthInsurance)
                .subtract(unemploymentInsurance)
                .subtract(personalIncomeTax)
                .subtract(otherDeductions)
                .setScale(SCALE, ROUNDING);

        // 9. Upsert payslip: tạo mới nếu chưa có, ghi đè nếu đã tồn tại (theo đúng nghiệp vụ trong tài liệu)
        Payslip payslip = payslipRepository
                .findByPayrollPeriod_IdAndEmployee_Id(period.getId(), contract.getEmployee().getId())
                .orElseGet(() -> Payslip.builder()
                        .company(contract.getCompany())
                        .payrollPeriod(period)
                        .employee(contract.getEmployee())
                        .build());

        payslip.setBaseSalary(contract.getBaseSalary());
        payslip.setStandardWorkDays(standardWorkDays);
        payslip.setActualWorkDays(actualWorkDays);
        payslip.setPaidLeaveDays(paidLeaveDays);
        payslip.setUnpaidLeaveDays(unpaidLeaveDays);
        payslip.setSalaryByWorkDays(salaryByWorkDays);
        payslip.setTotalAllowances(totalAllowances);
        payslip.setBonusKpi(bonusKpi);
        payslip.setBonusProject(bonusProject);
        payslip.setOtherBonuses(otherBonuses);
        payslip.setOtHours(otHours);
        payslip.setOtSalary(otSalary);
        payslip.setGrossSalary(grossSalary);
        payslip.setDeductionSocialInsurance(socialInsurance);
        payslip.setDeductionHealthInsurance(healthInsurance);
        payslip.setDeductionUnemploymentInsurance(unemploymentInsurance);
        payslip.setPersonalIncomeTax(personalIncomeTax);
        payslip.setOtherDeductions(otherDeductions);
        payslip.setNetSalary(netSalary);
        payslip.setStatus(PayslipStatus.DRAFT);

        payslipRepository.save(payslip);
    }

    @Override
    @Transactional
    public void approve(Long payrollPeriodId) {
        PayrollPeriod period = payrollPeriodRepository.findById(payrollPeriodId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYROLL_PERIOD_NOT_FOUND));

        if (period.getStatus() != PayrollPeriodStatus.DRAFT) {
            throw new AppException(ErrorCode.PAYROLL_PERIOD_NOT_APPROVABLE);
        }

        period.setStatus(PayrollPeriodStatus.APPROVED);
        payrollPeriodRepository.save(period);

        // Khóa toàn bộ payslip: DRAFT -> SENT (gửi thông báo nên tách sang NotificationService riêng)
        payslipRepository.bulkUpdateStatusByPeriod(payrollPeriodId, PayslipStatus.DRAFT, PayslipStatus.SENT);
    }

    @Override
    @Transactional
    public void pay(Long payrollPeriodId) {
        PayrollPeriod period = payrollPeriodRepository.findById(payrollPeriodId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYROLL_PERIOD_NOT_FOUND));

        if (period.getStatus() != PayrollPeriodStatus.APPROVED) {
            throw new AppException(ErrorCode.PAYROLL_PERIOD_NOT_PAYABLE);
        }

        period.setStatus(PayrollPeriodStatus.PAID);
        payrollPeriodRepository.save(period);

        payslipRepository.bulkMarkAsPaid(payrollPeriodId, PayslipStatus.SENT, PayslipStatus.PAID, LocalDate.now());
    }
    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
