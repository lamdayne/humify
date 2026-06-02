package com.lamdayne.humify.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken implements Serializable {

    @Id
    private String token;

    private Long userId;

    private Instant expiryDate;

    private Boolean revoked;

    @CreationTimestamp
    private Instant createdAt;

}
