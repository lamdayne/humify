package com.lamdayne.humify.payroll.service;

import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.payroll.dto.request.CreatePayrollPeriodRequest;
import com.lamdayne.humify.payroll.dto.response.PayrollPeriodResponse;

public interface PayrollPeriodService {

    PayrollPeriodResponse createPayrollPeriod(Long companyId, CreatePayrollPeriodRequest request);
    PageResponse<PayrollPeriodResponse> getPayrollPeriods(Long companyId, int page, int size);
    int calculatePayroll(Long id, Long companyId); // Trả về số lượng nhân viên được tính lương
}
