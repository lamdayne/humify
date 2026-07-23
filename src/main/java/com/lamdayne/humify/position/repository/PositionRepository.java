package com.lamdayne.humify.position.repository;

import com.lamdayne.humify.position.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    boolean existsByIdAndCompanyId(Long id, Long companyId);

    Optional<Position> findByName(String name);
}
