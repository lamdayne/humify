package com.lamdayne.humify.employee.repository;

import com.lamdayne.humify.employee.entity.EmployeeWorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeWorkExperienceRepository extends JpaRepository<EmployeeWorkExperience, Long> {
}
