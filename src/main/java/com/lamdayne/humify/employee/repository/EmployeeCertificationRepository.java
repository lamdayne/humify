package com.lamdayne.humify.employee.repository;

import com.lamdayne.humify.employee.entity.EmployeeCertification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeCertificationRepository extends JpaRepository<EmployeeCertification, Long> {
}
