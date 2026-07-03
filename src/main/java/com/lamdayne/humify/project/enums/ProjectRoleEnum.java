package com.lamdayne.humify.project.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProjectRoleEnum {
    MANAGER("Project Manager", ProjectRoleCode.MANAGER, "Can do most things"),
    MEMBER("Member", ProjectRoleCode.MEMBER, "Can add, edit and collaborate on all work"),
    VIEWER("VIEWER", ProjectRoleCode.VIEWER, "Can search through, view and comment on your team work"),

    ;

    private final String name;
    private final ProjectRoleCode code;
    private final String description;

}
