package com.lamdayne.humify.employee.repository;

import com.lamdayne.humify.employee.entity.EmployeeContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeContractRepository extends JpaRepository<EmployeeContract, Long> {
}
