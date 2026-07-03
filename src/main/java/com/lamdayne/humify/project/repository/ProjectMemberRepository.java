package com.lamdayne.humify.project.repository;

import com.lamdayne.humify.project.entity.ProjectMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    Page<ProjectMember> findAllByProjectId(Long projectId, Pageable pageable);

    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);
}
