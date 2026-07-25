package com.lamdayne.humify.project.repository;

import com.lamdayne.humify.project.entity.Project;
import com.lamdayne.humify.project.enums.ProjectMemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByCompanyIdAndKey(Long companyId, String key);

    Page<Project> findByCompanyId(Long companyId, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.company.id = :companyId AND p.id IN (SELECT pm.project.id FROM ProjectMember pm WHERE pm.user.id = :userId AND pm.status = :status) AND p.deletedAt IS NULL")
    Page<Project> findByCompanyIdAndMemberUserId(@Param("companyId") Long companyId, @Param("userId") Long userId, @Param("status") ProjectMemberStatus status, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE projects SET issue_counter = issue_counter + 1 WHERE id = :projectId", nativeQuery = true)
    void incrementIssueCounter(@Param("projectId") Long projectId);

    @Query(value = "SELECT issue_counter FROM projects WHERE id = :projectId", nativeQuery = true)
    Long getIssueCounter(@Param("projectId") Long projectId);

}
