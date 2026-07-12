package com.lamdayne.humify.attendance.repository;

import com.lamdayne.humify.attendance.entity.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkShiftRepository extends JpaRepository<WorkShift, Long>, JpaSpecificationExecutor<WorkShift> {

    Optional<WorkShift> findByIdAndCompanyIdAndDeletedAtIsNull(Long id, Long companyId);

    boolean existsByCompanyIdAndShiftCodeAndDeletedAtIsNull(Long companyId, String shiftCode);
}