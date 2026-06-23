package com.lamdayne.humify.project.repository;

import com.lamdayne.humify.project.entity.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRoleRepository extends JpaRepository<ProjectRole, Long> {
}
