package com.lamdayne.humify.project.repository;

import com.lamdayne.humify.project.entity.ProjectInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Long> {

    @Query("SELECT i FROM ProjectInvitation i " +
            "JOIN FETCH i.project p " +
            "JOIN FETCH p.company " +
            "JOIN FETCH i.projectRole " +
            "WHERE i.token = :token")
    Optional<ProjectInvitation> findByToken(@Param("token") String token);
}