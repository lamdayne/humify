package com.lamdayne.humify.company.entity;

import com.lamdayne.humify.common.base.BaseEntity;
import com.lamdayne.humify.company.enums.CompanyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "companies")
public class Company extends BaseEntity implements Serializable {

    @Column(nullable = false, unique = true, length = 50)
    private String companyCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String field;

    private String website;

    @Column(nullable = false, length = 20, unique = true)
    private String taxCode;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "representative_email", nullable = false, unique = true)
    private String email;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "company_status", nullable = false)
    private CompanyStatus status = CompanyStatus.PENDING;

}
