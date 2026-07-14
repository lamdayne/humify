package com.lamdayne.humify.payroll.service;

import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.payroll.dto.request.CreatePayrollPeriodRequest;
import com.lamdayne.humify.payroll.dto.response.PayrollPeriodResponse;

public interface PayrollPeriodService {

    PayrollPeriodResponse createPayrollPeriod(Long companyId, CreatePayrollPeriodRequest request);
    PageResponse<PayrollPeriodResponse> getPayrollPeriods(Long companyId, int page, int size);


    /**
     * Chạy tính toán lương cho toàn bộ nhân viên có hợp đồng ACTIVE trong kỳ lương.
     * Tạo mới payslip nếu chưa có, tính toán lại (ghi đè) nếu đã tồn tại.
     *
     * @return số lượng phiếu lương đã được tạo/tính lại
     */
    int calculate(Long payrollPeriodId, Long companyId);

    /** Duyệt bảng lương: DRAFT -> APPROVED, đồng thời chuyển toàn bộ payslip DRAFT -> SENT. */
    void approve(Long payrollPeriodId, Long companyId);

    /**
     * Xác nhận đã thanh toán: APPROVED -> PAID, đồng thời chuyển toàn bộ payslip SENT -> PAID
     * và ghi nhận payment_date.
     */
    void pay(Long payrollPeriodId, Long companyId);
}
