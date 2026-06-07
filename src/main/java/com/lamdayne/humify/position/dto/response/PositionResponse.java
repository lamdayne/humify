package com.lamdayne.humify.position.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@Builder
public class PositionResponse implements Serializable {
    private Long id;
    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}