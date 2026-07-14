package com.lamdayne.humify.payroll.repository;

import com.lamdayne.humify.payroll.entity.PayrollPeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayrollPeriodRepository extends JpaRepository<PayrollPeriod, Long> {

    // Kiểm tra trùng lặp kỳ lương theo tháng và năm của công ty
    boolean existsByCompanyIdAndMonthAndYear(Long companyId, Integer month, Integer year);

    // Lấy danh sách kỳ lương phân trang theo công ty
    Page<PayrollPeriod> findByCompanyIdOrderByYearDescMonthDesc(Long companyId, Pageable pageable);

    // Lấy chi tiết để tính lương
    Optional<PayrollPeriod> findByIdAndCompanyId(Long id, Long companyId);
}
