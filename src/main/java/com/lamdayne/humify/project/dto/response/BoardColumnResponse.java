package com.lamdayne.humify.project.dto.response;

import com.lamdayne.humify.project.enums.ColumnCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardColumnResponse {

    private Long id;
    private String name;
    private int position;
    private ColumnCategory category;
}