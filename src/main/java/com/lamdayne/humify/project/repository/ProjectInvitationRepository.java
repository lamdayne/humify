package com.lamdayne.humify.project.repository;

import com.lamdayne.humify.project.entity.ProjectInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Long> {
}
