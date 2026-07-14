package com.lamdayne.humify.employee.repository;

import com.lamdayne.humify.employee.entity.EmployeeWorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeWorkExperienceRepository extends JpaRepository<EmployeeWorkExperience, Long> {
    List<EmployeeWorkExperience> findByEmployeeId(Long employeeId);

    Optional<EmployeeWorkExperience> findByIdAndEmployeeId(Long id, Long employeeId);
}
