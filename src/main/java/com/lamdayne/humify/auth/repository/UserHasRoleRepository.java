package com.lamdayne.humify.auth.repository;

import com.lamdayne.humify.auth.entity.UserHasRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Modifying
    @Query("DELETE FROM UserHasRole uhr WHERE uhr.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query("SELECT uhr.role.name FROM UserHasRole uhr WHERE uhr.user.id = :userId")
    List<String> findAllRoleNameByUserId(@Param("userId") Long userId);
}
