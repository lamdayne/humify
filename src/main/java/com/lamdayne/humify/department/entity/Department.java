package com.lamdayne.humify.department.entity;

import com.lamdayne.humify.common.base.BaseEntity;
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
@Table(name = "departments")
public class Department extends BaseEntity implements Serializable {
}
