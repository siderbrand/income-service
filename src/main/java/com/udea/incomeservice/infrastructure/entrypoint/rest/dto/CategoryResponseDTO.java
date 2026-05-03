package com.udea.incomeservice.infrastructure.entrypoint.rest.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private String type;
}
