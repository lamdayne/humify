package com.lamdayne.humify.auth.repository;

import com.lamdayne.humify.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    boolean existsByCompanyIdAndName(Long companyId, String name);

}
