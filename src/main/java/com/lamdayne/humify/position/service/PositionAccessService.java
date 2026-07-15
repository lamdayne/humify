package com.lamdayne.humify.position.service;

import com.lamdayne.humify.common.base.BaseAccessService;
import com.lamdayne.humify.position.entity.Position;

import java.util.List;
import java.util.Optional;

public interface PositionAccessService extends BaseAccessService<Position, Long> {

    boolean existsByIdAndCompanyId(Long id, Long companyId);

    Optional<Position> findByName(String name);

    List<Position> findAll();

}
