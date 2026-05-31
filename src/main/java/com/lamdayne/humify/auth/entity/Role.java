package com.lamdayne.humify.auth.entity;

import com.lamdayne.humify.common.base.BaseEntity;
import com.lamdayne.humify.company.entity.Company;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role extends BaseEntity implements Serializable {

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

}
