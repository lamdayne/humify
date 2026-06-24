package com.lamdayne.humify.project.service.impl;

import com.lamdayne.humify.project.entity.ProjectRole;
import com.lamdayne.humify.project.enums.ProjectRoleEnum;
import com.lamdayne.humify.project.repository.ProjectRoleRepository;
import com.lamdayne.humify.project.service.ProjectRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j(topic = "PROJECT_ROLE_SERVICE")
public class ProjectRoleServiceImpl implements ProjectRoleService {

    private final ProjectRoleRepository projectRoleRepository;

    @Override
    public void initProjectRole() {
        Set<String> existing = new HashSet<>(projectRoleRepository.findAllNames());

        List<ProjectRole> toInsert = Arrays.stream(ProjectRoleEnum.values())
                .filter(pr -> !existing.contains(pr.name()))
                .map(pr -> ProjectRole.builder()
                        .name(pr.name())
                        .code(pr.getCode().name())
                        .description(pr.getDescription())
                        .build()
                ).toList();

        if (!toInsert.isEmpty()) {
            projectRoleRepository.saveAll(toInsert);
            log.info("Insert new {} project roles", toInsert.size());
        }
    }
}
