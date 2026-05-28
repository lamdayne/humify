package com.lamdayne.humify.company.repository;

import com.lamdayne.humify.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByTaxCode(String taxCode);

    boolean existsByEmail(String email);

    Optional<Company> findByCompanyCode(String companyCode);
}
