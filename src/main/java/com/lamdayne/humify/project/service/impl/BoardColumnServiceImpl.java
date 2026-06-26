package com.lamdayne.humify.project.service.impl;

import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.project.dto.request.CreateColumnRequest;
import com.lamdayne.humify.project.dto.request.ReorderColumnsRequest;
import com.lamdayne.humify.project.dto.request.UpdateColumnRequest;
import com.lamdayne.humify.project.dto.response.BoardColumnResponse;
import com.lamdayne.humify.project.entity.BoardColumn;
import com.lamdayne.humify.project.entity.Project;
import com.lamdayne.humify.project.enums.ColumnCategory;
import com.lamdayne.humify.project.mapper.BoardColumnMapper;
import com.lamdayne.humify.project.repository.BoardColumnRepository;
import com.lamdayne.humify.project.repository.ProjectRepository;
import com.lamdayne.humify.project.service.BoardColumnService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardColumnServiceImpl implements BoardColumnService {

    private final ProjectRepository projectRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final BoardColumnMapper boardColumnMapper;

    @Override
    public List<BoardColumnResponse> getColumns(Long projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        return boardColumnRepository.findAllByProjectIdOrderByPositionAsc(projectId)
                .stream()
                .map(boardColumnMapper::toBoardColumnResponse)
                .toList();
    }

    @Override
    @Transactional
    public BoardColumnResponse createColumn(Long projectId, CreateColumnRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        int newPosition = boardColumnRepository.findTopByProjectIdOrderByPositionDesc(projectId)
                .map(column -> column.getPosition() + 1)
                .orElse(0);

        BoardColumn boardColumn = BoardColumn.builder()
                .project(project)
                .name(request.getName())
                .category(ColumnCategory.valueOf(request.getCategory()))
                .position(newPosition)
                .build();

        return boardColumnMapper.toBoardColumnResponse(boardColumnRepository.save(boardColumn));
    }

    @Override
    @Transactional
    public BoardColumnResponse updateColumn(Long id, UpdateColumnRequest request) {
        BoardColumn boardColumn = boardColumnRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COLUMN_NOT_FOUND));

        boardColumn.setName(request.getName());
        boardColumn.setCategory(ColumnCategory.valueOf(request.getCategory()));

        return boardColumnMapper.toBoardColumnResponse(boardColumnRepository.save(boardColumn));
    }

    @Override
    @Transactional
    public List<BoardColumnResponse> reorderColumns(Long projectId, ReorderColumnsRequest request) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        List<BoardColumn> columns = boardColumnRepository.findAllByProjectIdOrderByPositionAsc(projectId);

        if (request.getColumnIds() == null || request.getColumnIds().isEmpty()) {
            throw new AppException(ErrorCode.COLUMN_REORDER_INVALID);
        }

        if (request.getColumnIds().size() != columns.size()) {
            throw new AppException(ErrorCode.COLUMN_REORDER_INVALID);
        }

        Map<Long, BoardColumn> columnMap = columns.stream()
                .collect(Collectors.toMap(BoardColumn::getId, column -> column));

        Set<Long> uniqueIds = new HashSet<>(request.getColumnIds());
        if (uniqueIds.size() != request.getColumnIds().size()) {
            throw new AppException(ErrorCode.COLUMN_REORDER_INVALID);
        }

        for (Long columnId : request.getColumnIds()) {
            if (!columnMap.containsKey(columnId)) {
                throw new AppException(ErrorCode.COLUMN_REORDER_INVALID);
            }
        }

        for (int i = 0; i < request.getColumnIds().size(); i++) {
            BoardColumn column = columnMap.get(request.getColumnIds().get(i));
            column.setPosition(i);
        }

        List<BoardColumn> savedColumns = boardColumnRepository.saveAll(columns);

        return savedColumns.stream()
                .sorted(Comparator.comparingInt(BoardColumn::getPosition))
                .map(boardColumnMapper::toBoardColumnResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteColumn(Long id, Long moveToColumnId) {
        BoardColumn boardColumn = boardColumnRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COLUMN_NOT_FOUND));

        Long projectId = boardColumn.getProject().getId();
        int deletedPosition = boardColumn.getPosition();

        List<BoardColumn> columns = boardColumnRepository.findAllByProjectIdOrderByPositionAsc(projectId);
        if (columns.size() <= 1) {
            throw new AppException(ErrorCode.COLUMN_POSITION_INVALID);
        }
        if (moveToColumnId != null) {
            BoardColumn targetColumn = boardColumnRepository.findById(moveToColumnId)
                    .orElseThrow(() -> new AppException(ErrorCode.COLUMN_NOT_FOUND));

            if (Objects.equals(targetColumn.getId(), boardColumn.getId())) {
                throw new AppException(ErrorCode.COLUMN_REORDER_INVALID);
            }
            if (!Objects.equals(targetColumn.getProject().getId(), projectId)) {
                throw new AppException(ErrorCode.COLUMN_REORDER_INVALID);
            }

        }


        boardColumnRepository.delete(boardColumn);

        List<BoardColumn> remainingColumns = boardColumnRepository.findAllByProjectIdOrderByPositionAsc(projectId);

        for (BoardColumn column : remainingColumns) {
            if (column.getPosition() > deletedPosition) {
                column.setPosition(column.getPosition() - 1);
            }
        }

        boardColumnRepository.saveAll(remainingColumns);
    }
}