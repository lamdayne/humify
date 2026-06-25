package com.lamdayne.humify.project.dto.request;

import com.lamdayne.humify.common.validator.EnumValue;
import com.lamdayne.humify.project.enums.ColumnCategory;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateColumnRequest implements Serializable {

    @NotBlank(message = "COLUMN_NAME_REQUIRED")
    private String name;

    @EnumValue(
            name = "column_category",
            message = "COLUMN_CATEGORY_INVALID",
            enumClass = ColumnCategory.class
    )
    private String category;
}