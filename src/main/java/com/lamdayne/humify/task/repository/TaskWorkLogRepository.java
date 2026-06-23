package com.lamdayne.humify.task.repository;

import com.lamdayne.humify.task.entity.TaskWorkLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskWorkLogRepository extends JpaRepository<TaskWorkLog, Long> {
}
