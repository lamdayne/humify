package com.lamdayne.humify.task.repository;

import com.lamdayne.humify.task.entity.TaskWorkLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskWorkLogRepository extends JpaRepository<TaskWorkLog, Long> {
    List<TaskWorkLog> findByTaskIdOrderByLoggedAtDesc(Long taskId);

    @Query("SELECT w FROM TaskWorkLog w WHERE w.user.id = :userId")
    List<TaskWorkLog> findByUser_Id(Long userId);
}
