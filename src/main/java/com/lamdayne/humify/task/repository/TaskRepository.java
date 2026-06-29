package com.lamdayne.humify.task.repository;

import com.lamdayne.humify.task.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT MAX(t.position) FROM Task t WHERE t.column.id = :columnId")
    Double findMaxPositionByColumnId(@Param("columnId") Long columnId);

    @EntityGraph(attributePaths = {
            "project", "reporter", "assignee", "sprint", "column", "parent"
    })
    Page<Task> findAllByProjectId(Long projectId, Pageable pageable);

    @EntityGraph(attributePaths = {
            "project", "sprint", "column", "parent", "reporter", "assignee"
    })
    Optional<Task> findDetailById(Long id);

    @EntityGraph(attributePaths = {"column", "assignee"})
    List<Task> findByParentId(Long parentId);

    @Query("SELECT t FROM Task t WHERE t.assignee.id = :assigneeId")
    List<Task> findByAssignee_Id(Long assigneeId);
}
