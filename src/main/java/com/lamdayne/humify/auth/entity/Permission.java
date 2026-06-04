package com.lamdayne.humify.auth.entity;

import com.lamdayne.humify.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity implements Serializable {

    @Column(nullable = false)
    private String name;

    private String description;

    private String module;
}
