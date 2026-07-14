package com.lamdayne.humify.employee.repository;

import com.lamdayne.humify.employee.entity.EmployeeIdDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeIdDocumentRepository extends JpaRepository<EmployeeIdDocument, Long> {

    @Query("SELECT d FROM EmployeeIdDocument d WHERE d.employee.id = :employeeId AND d.employee.company.id = :companyId")
    List<EmployeeIdDocument> findAllByEmployeeIdAndCompanyId(@Param("employeeId") Long employeeId, @Param("companyId") Long companyId);

    @Query("SELECT d FROM EmployeeIdDocument d WHERE d.id = :id AND d.employee.id = :employeeId AND d.employee.company.id = :companyId")
    Optional<EmployeeIdDocument> findByIdAndEmployeeIdAndCompanyId(@Param("id") Long id, @Param("employeeId") Long employeeId, @Param("companyId") Long companyId);
}