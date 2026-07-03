package com.lamdayne.humify.project.dto.request;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderColumnsRequest implements Serializable {

    private List<Long> columnIds;
}