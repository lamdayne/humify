package com.lamdayne.humify.payroll.repository;

import com.lamdayne.humify.payroll.entity.PayrollPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollPeriodRepository extends JpaRepository<PayrollPeriod, Long> {
}
