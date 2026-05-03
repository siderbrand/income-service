package com.udea.incomeservice.infrastructure.entrypoint.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta estándar de error")
public class ErrorResponseDTO {

    @Schema(description = "Código de estado HTTP", example = "400")
    private int errorCode;

    @Schema(description = "Mensaje de error", example = "El monto debe ser mayor a cero")
    private String message;

    @Schema(description = "Detalle adicional del error")
    private List<String> details;
}
