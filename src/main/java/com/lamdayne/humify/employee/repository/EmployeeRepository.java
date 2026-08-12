package com.lamdayne.humify.employee.repository;

import com.lamdayne.humify.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmployeeCode(String employeeCode);

    Optional<Employee> findFirstByCompanyIdAndEmployeeCodeStartingWithOrderByIdDesc(Long companyId, String prefix);

    boolean existsByCompanyIdAndEmail(Long companyId, String email);

    Optional<Employee> findByEmailAndCompanyId(String email, Long companyId);

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByEmployeeCodeAndCompanyCompanyCode(String employeeCode, String companyCode);

    Optional<Employee> findByEmployeeCodeAndCompanyId(String employeeCode, Long companyId);

    Optional<Employee> findByEmployeeCodeIgnoreCase(String employeeCode);

    List<Employee> findAllByEmployeeCodeIgnoreCase(String employeeCode);

    Optional<Employee> findByNfcCardUid(String nfcCardUid);

    Optional<Employee> findByNfcCardUidIgnoreCase(String nfcCardUid);

    List<Employee> findAllByNfcCardUidIgnoreCase(String nfcCardUid);
}
