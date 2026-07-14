package com.lamdayne.humify.payroll.service;

import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.payroll.dto.request.UpdatePayslipRequest;
import com.lamdayne.humify.payroll.dto.response.MyPayslipResponse;
import com.lamdayne.humify.payroll.dto.response.PayslipResponse;
import com.lamdayne.humify.payroll.enums.PayslipStatus;

public interface PayslipService {


    /** GET /payroll-periods/{id}/payslips — dành cho HR. */
    PageResponse<PayslipResponse> getPayslipsByPeriod(
            Long payrollPeriodId, Long employeeId, PayslipStatus status, int page, int size, String... sorts
    );

    /** PUT /payslips/{id} — HR điều chỉnh thủ công, chỉ khi kỳ lương đang DRAFT. */
    PayslipResponse updatePayslip(Long payslipId, Long companyId, UpdatePayslipRequest request);

    /** GET /employees/my-payslips — nhân viên tự tra cứu, chỉ thấy SENT/PAID. */
    PageResponse<MyPayslipResponse> getMyPayslips(Long employeeId, Long companyId, Integer year, int page, int size, String... sorts);
}
