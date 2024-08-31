package teste.tgid_bruno.dtos;

import jakarta.validation.constraints.Positive;

public record CreditRequestDto(
        String id_empresa,
        @Positive Double value) {

}
