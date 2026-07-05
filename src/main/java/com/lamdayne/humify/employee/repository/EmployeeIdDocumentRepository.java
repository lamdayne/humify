package com.lamdayne.humify.employee.repository;

import com.lamdayne.humify.employee.entity.EmployeeIdDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeIdDocumentRepository extends JpaRepository<EmployeeIdDocument, Long> {
}
