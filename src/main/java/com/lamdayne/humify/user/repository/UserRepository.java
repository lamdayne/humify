package com.lamdayne.humify.user.repository;

import com.lamdayne.humify.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndCompanyId(String email, Long companyId);

    Optional<User> findByEmailAndCompanyIsNull(String email);

    List<User> findAllByEmail(String email);

    boolean existsByEmailAndCompanyId(String email, Long companyId);

    boolean existsByEmailAndCompanyIsNull(String email);
}
