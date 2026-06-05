package com.lamdayne.humify.auth.repository;

import com.lamdayne.humify.auth.entity.RoleHasPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleHasPermissionRepository extends JpaRepository<RoleHasPermission,Long> {

    @Query("select rhp from RoleHasPermission rhp join fetch rhp.permission where rhp.role.id = :roleId")
    List<RoleHasPermission> findByRoleId(@Param("roleId") Long roleId);

    void deleteByRoleId(Long roleId);
}
