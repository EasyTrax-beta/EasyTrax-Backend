package com.easytrax.easytraxbackend.project.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProjectStatus {
    IN_PROGRESS("진행중"),
    COMPLETED("완료"),
    ON_HOLD("보류");

    private final String description;
}