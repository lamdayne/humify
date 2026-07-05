package com.lamdayne.humify.employee.repository;

import com.lamdayne.humify.employee.entity.EmployeeEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeEducationRepository extends JpaRepository<EmployeeEducation, Long> {
}
