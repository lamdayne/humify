package com.lamdayne.humify.company.repository;

import com.lamdayne.humify.company.entity.CompanyVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyVerificationRepository extends JpaRepository<CompanyVerification, Long> {

    Optional<CompanyVerification> findByToken(String token);

}
