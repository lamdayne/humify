package com.lamdayne.humify.position.repository;

import com.lamdayne.humify.position.entity.Position;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    Page<Position> findByCompanyId(Long companyId, Pageable pageable);
}
