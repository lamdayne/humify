package com.lamdayne.humify.project.repository;

import com.lamdayne.humify.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    boolean existsByCompanyIdAndKey(Long companyId, String key);

    Page<Project> findProjectByCompany_Id(Long companyId,Pageable pageable);


}
