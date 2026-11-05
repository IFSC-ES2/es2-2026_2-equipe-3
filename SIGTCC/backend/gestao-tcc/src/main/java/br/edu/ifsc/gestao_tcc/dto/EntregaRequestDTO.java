package br.edu.ifsc.gestao_tcc.dto;

import br.edu.ifsc.gestao_tcc.model.TipoEntrega;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record EntregaRequestDTO(
        @NotNull(message = "O tipo de entrega é obrigatório.")
        TipoEntrega tipo,

        @NotBlank(message = "A URL do documento é obrigatória.")
        @Size(max = 2048, message = "A URL do documento deve ter no máximo 2048 caracteres.")
        @URL(protocol = "https", regexp = "(?i)^$|^https://[^\\s/?#]+\\S*$",
                message = "A URL do documento deve ser válida e utilizar o protocolo https.")
        String urlDocumento
) {
}
