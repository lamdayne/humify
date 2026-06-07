package com.lamdayne.humify.department.repository;

import com.lamdayne.humify.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByBranchId(Long branchId);

    boolean existsByIdAndBranchId(Long id, Long branchId);
}
