package com.lamdayne.humify.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveBalance extends JpaRepository<LeaveBalance, Long> {
}
