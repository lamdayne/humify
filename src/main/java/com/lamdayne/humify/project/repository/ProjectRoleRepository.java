package com.lamdayne.humify.project.repository;

import com.lamdayne.humify.project.entity.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRoleRepository extends JpaRepository<ProjectRole, Long> {

    @Query("SELECT pr.name FROM ProjectRole pr")
    List<String> findAllNames();

    Optional<ProjectRole> findByName(String name);
}
