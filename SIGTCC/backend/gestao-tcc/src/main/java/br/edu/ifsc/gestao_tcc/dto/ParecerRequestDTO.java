package br.edu.ifsc.gestao_tcc.dto;

import br.edu.ifsc.gestao_tcc.model.StatusEntrega;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ParecerRequestDTO(
        @NotNull(message = "O resultado do parecer é obrigatório.")
        StatusEntrega resultado,

        @Size(max = 2000, message = "O comentário deve ter no máximo 2000 caracteres.")
        String comentario
) {
}
