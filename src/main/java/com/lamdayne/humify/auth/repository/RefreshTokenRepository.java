package com.lamdayne.humify.auth.repository;

import com.lamdayne.humify.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    @Modifying
    @Query("""
                UPDATE RefreshToken rt
                SET rt.revoked = true
                WHERE rt.token = :token
                  AND rt.revoked = false
                  AND rt.expiryDate > :now
            """)
    int revokeIfValid(String token, Instant now);

}
