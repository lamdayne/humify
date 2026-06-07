package com.lamdayne.humify.employee.repository;

import com.lamdayne.humify.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByCompanyIdAndEmployeeCode(Long companyId, String employeeCode);

    Optional<Employee> findByEmployeeCode(String employeeCode);
}
