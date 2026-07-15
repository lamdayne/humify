package com.lamdayne.humify.department.repository;

import com.lamdayne.humify.department.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Page<Department> findByBranchId(Long branchId, Pageable pageable);

    boolean existsByIdAndBranchId(Long id, Long branchId);

    Optional<Department> findByNameAndBranchId(String name, Long branchId);

    List<Department> findByBranchId(Long branchId);
}
