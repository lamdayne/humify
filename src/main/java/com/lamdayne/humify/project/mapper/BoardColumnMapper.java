package com.lamdayne.humify.project.mapper;

import com.lamdayne.humify.project.dto.response.BoardColumnResponse;
import com.lamdayne.humify.project.entity.BoardColumn;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BoardColumnMapper {

    BoardColumnResponse toBoardColumnResponse(BoardColumn boardColumn);
}