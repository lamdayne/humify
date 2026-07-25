package com.lamdayne.humify.task.repository;

import com.lamdayne.humify.task.entity.TaskActivity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskActivityRepository extends JpaRepository<TaskActivity, Long> {

    @EntityGraph(attributePaths = {"user", "user.employee", "task", "task.column"})
    List<TaskActivity> findByTaskIdOrderByCreatedAtDesc(Long taskId);

    @EntityGraph(attributePaths = {"user", "user.employee", "task", "task.column"})
    List<TaskActivity> findByTaskProjectIdOrderByCreatedAtDesc(Long projectId, Pageable pageable);

}