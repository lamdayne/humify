package com.lamdayne.humify.task.repository;

import com.lamdayne.humify.task.entity.Task;
import com.lamdayne.humify.task.enums.TaskType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

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

    @EntityGraph(attributePaths = {
            "project", "column", "assignee", "assignee.employee", "reporter", "reporter.employee", "parent"
    })
    List<Task> findByProjectId(Long projectId);

    @Query("""
    SELECT COUNT(t)
    FROM Task t
    WHERE t.assignee.id = :userId
      AND t.deletedAt IS NULL
      AND t.dueDate >= :start
      AND t.dueDate < :endExclusive
      AND t.type <> :excludedType
      AND t.parent IS NULL
""")
    long countEligibleTasks(
            @Param("userId") Long userId,
            @Param("start") Instant start,
            @Param("endExclusive") Instant endExclusive,
             @Param("excludedType") TaskType excludedType

    );

    @Query("""
    SELECT COUNT(t)
    FROM Task t
    WHERE t.assignee.id = :userId
      AND t.deletedAt IS NULL
      AND t.dueDate >= :start
      AND t.dueDate < :endExclusive
      AND t.type <> :excludedType
      AND t.parent IS NULL
      AND t.completedAt IS NOT NULL
""")
    long countCompletedEligibleTasks(
            @Param("userId") Long userId,
            @Param("start") Instant start,
            @Param("endExclusive") Instant endExclusive,
             @Param("excludedType") TaskType excludedType

    );

    @Query("""
    SELECT COUNT(t)
    FROM Task t
    WHERE t.assignee.id = :userId
      AND t.deletedAt IS NULL
      AND t.dueDate >= :start
      AND t.dueDate < :endExclusive
      AND t.type <> :excludedType
      AND t.parent IS NULL
      AND t.completedAt IS NOT NULL
      AND t.completedAt <= t.dueDate
""")
    long countOnTimeEligibleTasks(
            @Param("userId") Long userId,
            @Param("start") Instant start,
            @Param("endExclusive") Instant endExclusive,
             @Param("excludedType") TaskType excludedType

    );

    @EntityGraph(attributePaths = {
            "project", "reporter", "assignee", "sprint", "column", "parent"
    })
    Page<Task> findAll(Specification<Task> specification, Pageable pageable);

}
