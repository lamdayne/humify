package com.lamdayne.humify.project.service;

import com.lamdayne.humify.project.dto.request.CreateColumnRequest;
import com.lamdayne.humify.project.dto.request.ReorderColumnsRequest;
import com.lamdayne.humify.project.dto.request.UpdateColumnRequest;
import com.lamdayne.humify.project.dto.response.BoardColumnResponse;
import com.lamdayne.humify.project.entity.BoardColumn;

import java.util.List;

public interface BoardColumnService {

    List<BoardColumnResponse> getColumns(Long projectId);

    BoardColumnResponse createColumn(Long projectId, CreateColumnRequest request);

    BoardColumnResponse updateColumn(Long id, UpdateColumnRequest request);

    List<BoardColumnResponse> reorderColumns(Long projectId, ReorderColumnsRequest request);

    void deleteColumn(Long id, Long moveToColumnId);

    BoardColumn findById(Long id);

}