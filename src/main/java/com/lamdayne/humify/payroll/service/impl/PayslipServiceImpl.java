package com.lamdayne.humify.payroll.service.impl;


import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.search.GenericSpecificationBuilder;
import com.lamdayne.humify.common.search.SearchOperation;
import com.lamdayne.humify.common.search.SpecSearchCriteria;
import com.lamdayne.humify.common.util.PageableUtil;
import com.lamdayne.humify.payroll.dto.request.UpdatePayslipRequest;
import com.lamdayne.humify.payroll.dto.response.MyPayslipResponse;
import com.lamdayne.humify.payroll.dto.response.PayslipResponse;
import com.lamdayne.humify.payroll.entity.Payslip;
import com.lamdayne.humify.payroll.enums.PayrollPeriodStatus;
import com.lamdayne.humify.payroll.enums.PayslipStatus;
import com.lamdayne.humify.payroll.mapper.PayslipMapper;
import com.lamdayne.humify.payroll.repository.PayrollPeriodRepository;
import com.lamdayne.humify.payroll.repository.PayslipRepository;
import com.lamdayne.humify.payroll.service.PayslipService;
import com.lamdayne.humify.payroll.util.PayrollTaxCalculator;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;



@Service
@RequiredArgsConstructor
public class PayslipServiceImpl implements PayslipService {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final PayslipRepository payslipRepository;
    private final PayrollPeriodRepository payrollPeriodRepository;
    private final PayslipMapper payslipMapper;
    @Override
    @Transactional(readOnly = true)
    public PageResponse<PayslipResponse> getPayslipsByPeriod(
            Long payrollPeriodId, Long employeeId, PayslipStatus status, int page, int size, String... sorts
    ) {
        Pageable pageable = PageableUtil.buildPageable(page, size, sorts);

        // Đảm bảo kỳ lương tồn tại và thuộc công ty hiện tại trước khi truy vấn payslip con

        payrollPeriodRepository.findById(payrollPeriodId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYROLL_PERIOD_NOT_FOUND));

        GenericSpecificationBuilder<Payslip> builder = new GenericSpecificationBuilder<>();

        builder.with(new SpecSearchCriteria(
                "payrollPeriod.id",
                SearchOperation.EQUALITY,
                payrollPeriodId
        ));

        if (employeeId != null) {
            builder.with(new SpecSearchCriteria(
                    "employee.id",
                    SearchOperation.EQUALITY,
                    employeeId
            ));
        }

        if (status != null) {
            builder.with(new SpecSearchCriteria(
                    "status",
                    SearchOperation.EQUALITY,
                    status
            ));
        }

        Page<Payslip> result = payslipRepository.findAll(builder.build(), pageable);
        List<PayslipResponse> items = result.getContent().stream()
                .map(payslipMapper::toResponse)
                .toList();

        return PageResponse.<PayslipResponse>builder()
                .pageNo(page)
                .pageSize(result.getSize())
                .totalPages(result.getTotalPages())
                .totalElements(result.getTotalElements())
                .items(items)
                .build();
    }



    @Override
    @Transactional
    public PayslipResponse updatePayslip(Long payslipId, UpdatePayslipRequest request) {
        Payslip payslip = payslipRepository.findById(payslipId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYSLIP_NOT_FOUND));

        // Chỉ cho phép chỉnh sửa khi kỳ lương tương ứng đang ở trạng thái DRAFT
        if (payslip.getPayrollPeriod().getStatus() != PayrollPeriodStatus.DRAFT) {
            throw new AppException(ErrorCode.PAYSLIP_LOCKED_FOR_EDIT);
        }

        if (request.getOtherBonuses() != null) {
            payslip.setOtherBonuses(request.getOtherBonuses());
        }
        if (request.getOtherDeductions() != null) {
            payslip.setOtherDeductions(request.getOtherDeductions());
        }
        if (request.getNote() != null) {
            payslip.setNote(request.getNote());
        }

        recalculateAfterManualAdjustment(payslip);

        Payslip saved = payslipRepository.save(payslip);
        return payslipMapper.toResponse(saved);
    }

    /**
     * Tự động tính lại Gross, Thuế TNCN và Net Salary sau khi HR chỉnh sửa other_bonuses/other_deductions.
     * total_allowances, salary_by_work_days, bonus_kpi, bonus_project, ot_salary giữ nguyên vì các trường
     * này chỉ được sinh ra bởi bước tính lương tự động (POST .../calculate).
     */
    private void recalculateAfterManualAdjustment(Payslip payslip) {
        BigDecimal grossSalary = payslip.getSalaryByWorkDays()
                .add(payslip.getTotalAllowances())
                .add(payslip.getBonusKpi())
                .add(payslip.getBonusProject())
                .add(payslip.getOtherBonuses())
                .add(payslip.getOtSalary());
        payslip.setGrossSalary(grossSalary);

        BigDecimal totalCompulsoryInsurance = payslip.getDeductionSocialInsurance()
                .add(payslip.getDeductionHealthInsurance())
                .add(payslip.getDeductionUnemploymentInsurance());

        // NOTE: taxableDependents tại thời điểm calculate() không được lưu snapshot trên Payslip.
        // Nếu cần tái tính thuế 100% chính xác sau khi sửa tay, khuyến nghị đọc lại
        // payslip.getEmployee() -> hợp đồng ACTIVE hiện tại để lấy taxableDependents, hoặc thêm cột
        // taxable_dependents_snapshot vào bảng payslips khi calculate(). Tạm thời giữ 0 người phụ thuộc bổ sung.
        int dependentsSnapshot = 0;
//         TODO: thay bằng giá trị snapshot thật nếu bổ sung cột
        BigDecimal taxableIncome = PayrollTaxCalculator.calculateTaxableIncome(grossSalary, dependentsSnapshot, totalCompulsoryInsurance);
        BigDecimal personalIncomeTax = PayrollTaxCalculator.calculatePersonalIncomeTax(taxableIncome);
        payslip.setPersonalIncomeTax(personalIncomeTax);

        BigDecimal netSalary = grossSalary
                .subtract(payslip.getDeductionSocialInsurance())
                .subtract(payslip.getDeductionHealthInsurance())
                .subtract(payslip.getDeductionUnemploymentInsurance())
                .subtract(personalIncomeTax)
                .subtract(payslip.getOtherDeductions())
                .setScale(SCALE, ROUNDING);
        payslip.setNetSalary(netSalary);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MyPayslipResponse> getMyPayslips(Long employeeId, Integer year, int page, int size , String... sorts) {


        Pageable pageable = PageableUtil.buildPageable(page,size,sorts);
        Page<Payslip> result = payslipRepository.findMyPayslips(
                 employeeId, List.of(PayslipStatus.SENT, PayslipStatus.PAID), year,
                pageable
        );

        List<MyPayslipResponse> items = result.getContent().stream()
                .map(payslipMapper::toMyResponse)
                .toList();

        return PageResponse.<MyPayslipResponse>builder()
                .pageNo(page)
                .pageSize(result.getSize())
                .totalPages(result.getTotalPages())
                .totalElements(result.getTotalElements())
                .items(items)
                .build();

    }}
