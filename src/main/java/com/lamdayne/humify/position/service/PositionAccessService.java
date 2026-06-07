package com.lamdayne.humify.position.service;

import com.lamdayne.humify.common.base.BaseAccessService;
import com.lamdayne.humify.position.entity.Position;

public interface PositionAccessService extends BaseAccessService<Position, Long> {

    boolean existsByIdAndCompanyId(Long id, Long companyId);

}
