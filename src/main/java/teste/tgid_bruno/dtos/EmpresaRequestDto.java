package teste.tgid_bruno.dtos;

import jakarta.validation.constraints.NotBlank;

public record EmpresaRequestDto(
        @NotBlank String cnpj,
        @NotBlank String nome
) {
}
