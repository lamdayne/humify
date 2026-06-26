package com.lamdayne.humify.auth.repository;

import com.lamdayne.humify.auth.entity.UserSocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSocialAccountRepository extends JpaRepository<UserSocialAccount, Long> {
    Optional<UserSocialAccount> findByProviderAndProviderIdAndCompanyId(String provider, String providerId, Long companyId);
}
