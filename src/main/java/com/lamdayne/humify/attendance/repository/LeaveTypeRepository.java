package com.lamdayne.humify.attendance.repository;

import com.lamdayne.humify.attendance.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {

    List<LeaveType> findAllByCompanyId(Long companyId);
    List<LeaveType> findByCompanyIdAndDeletedAtIsNull(Long companyId);
    boolean existsByCompanyIdAndCodeAndDeletedAtIsNull(Long companyId, String code);
    Optional<LeaveType> findByIdAndCompanyIdAndDeletedAtIsNull(Long id, Long companyId);

}
