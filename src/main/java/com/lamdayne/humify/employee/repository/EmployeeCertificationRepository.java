package com.lamdayne.humify.employee.repository;

import com.lamdayne.humify.employee.entity.EmployeeCertification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeCertificationRepository extends JpaRepository<EmployeeCertification, Long> {

    Optional<EmployeeCertification> findByIdAndEmployeeId(Long id, Long employeeId);

    Page<EmployeeCertification> findByEmployeeId(Long employeeId, Pageable pageable);
}
