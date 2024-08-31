package teste.tgid_bruno.dtos;

import jakarta.validation.constraints.NotBlank;

public record ClienteRequestDto(
        @NotBlank String cpf,
        @NotBlank String nome,
        @NotBlank String id_empresa) {

}
