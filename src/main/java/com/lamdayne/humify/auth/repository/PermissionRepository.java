package com.lamdayne.humify.auth.repository;

import com.lamdayne.humify.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission,Long> {

    @Query("select p.name from Permission p")
    List<String> findAllNames();

}
