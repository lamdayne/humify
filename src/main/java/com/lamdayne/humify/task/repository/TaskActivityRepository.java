package com.lamdayne.humify.task.repository;

import com.lamdayne.humify.task.entity.TaskActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskActivityRepository extends JpaRepository<TaskActivity, Long> {

    List<TaskActivity> findByTaskIdOrderByCreatedAtDesc(Long taskId);

}