package com.udea.incomeservice.infrastructure.entrypoint.rest.dto;

import com.udea.incomeservice.infrastructure.entrypoint.rest.EntryPointConstants;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequestDTO {

    @NotBlank(message = EntryPointConstants.CATEGORY_NAME_REQUIRED)
    private String name;

    @NotBlank(message = EntryPointConstants.CATEGORY_TYPE_REQUIRED)
    private String type;
}
