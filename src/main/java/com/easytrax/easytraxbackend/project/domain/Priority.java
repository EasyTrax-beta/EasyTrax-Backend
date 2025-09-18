package com.easytrax.easytraxbackend.project.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Priority {
    HIGH("높음"),
    MEDIUM("보통"),
    LOW("낮음");

    private final String description;
}