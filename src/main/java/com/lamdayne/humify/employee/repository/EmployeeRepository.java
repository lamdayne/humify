package com.lamdayne.humify.employee.repository;

import com.lamdayne.humify.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeCode(String employeeCode);

    Optional<Employee> findFirstByCompanyIdAndEmployeeCodeStartingWithOrderByIdDesc(Long companyId, String prefix);

    boolean existsByCompanyIdAndEmail(Long companyId, String email);
}
