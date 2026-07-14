package com.lamdayne.humify.employee.repository;

import com.lamdayne.humify.employee.entity.EmployeeEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeEducationRepository extends JpaRepository<EmployeeEducation, Long> {

    List<EmployeeEducation> findByEmployeeIdOrderByStartYearDesc(Long employeeId);
}
