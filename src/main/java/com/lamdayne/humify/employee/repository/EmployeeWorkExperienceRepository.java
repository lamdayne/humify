package com.lamdayne.humify.employee.repository;

import com.lamdayne.humify.employee.entity.EmployeeWorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeWorkExperienceRepository extends JpaRepository<EmployeeWorkExperience, Long> {
    List<EmployeeWorkExperience> findByEmployee_Id(Long employeeId);

    Optional<EmployeeWorkExperience> findByIdAndEmployee_Id(Long id, Long employeeId);
}
