package com.lamdayne.humify.auth.repository;

import com.lamdayne.humify.auth.entity.UserHasRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserHasRoleRepository extends JpaRepository<UserHasRole, Long> {

    @Query("""
            select p.name from UserHasRole uhr join RoleHasPermission rhp on uhr.role.id = rhp.role.id
            join Permission p on rhp.permission.id = p.id
            where uhr.user.id = :userId and (uhr.company.id = :companyId or (uhr.company is null and :companyId is null))
            and p.deletedAt is null
            """)
    List<String> findPermissionNameByUserIdAndCompanyId(@Param("userId") Long userId, @Param("companyId") Long companyId);

    boolean existsByRoleId(Long roleId);
}
