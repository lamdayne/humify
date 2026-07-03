package com.lamdayne.humify.project.repository;

import com.lamdayne.humify.project.entity.BoardColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardColumnRepository extends JpaRepository<BoardColumn, Long> {

    List<BoardColumn> findAllByProjectIdOrderByPositionAsc(Long projectId);

    Optional<BoardColumn> findTopByProjectIdOrderByPositionDesc(Long projectId);
}