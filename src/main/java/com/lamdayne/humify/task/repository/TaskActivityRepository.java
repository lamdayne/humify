package com.lamdayne.humify.task.repository;

import com.lamdayne.humify.task.enums.TaskActivityAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskActivityRepository extends JpaRepository<TaskActivityAction, String> {
}
