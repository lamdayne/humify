package com.lamdayne.humify.project.repository;

import com.lamdayne.humify.project.entity.Sprint;
import com.lamdayne.humify.project.enums.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {

    List<Sprint> findByProjectId(Long projectId);

    List<Sprint> findByProjectIdAndStatus(Long projectId, SprintStatus status);

    // Phục vụ cho việc kiểm tra xem Project đã có Sprint ACTIVE nào chưa
    boolean existsByProjectIdAndStatus(Long projectId, SprintStatus status);
}
