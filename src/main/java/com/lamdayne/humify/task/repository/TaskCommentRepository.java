package com.lamdayne.humify.task.repository;

import com.lamdayne.humify.task.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {

    List<TaskComment> findByTaskIdOrderByCreatedAtAsc(Long taskId);

    @Modifying
    @Query("DELETE FROM TaskComment c WHERE c.parent.id = :parentId")
    void deleteByParentId(Long parentId);
}
