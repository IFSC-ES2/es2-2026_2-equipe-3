package br.edu.ifsc.gestao_tcc.dto;

import br.edu.ifsc.gestao_tcc.model.TipoEntrega;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * Corpo da requisição de submissão de entregas por link externo (ADR-0013).
 * <p>
 * Não há upload de arquivo: o aluno informa apenas a URL pública (somente leitura)
 * do documento/repositório. A anotação {@link URL} com {@code protocol = "https"}
 * garante, no próprio Bean Validation, que apenas links seguros (HTTPS) sejam aceitos,
 * sem a necessidade de nenhuma infraestrutura adicional de armazenamento.
 */
@Data
public class SubmissaoRequestDTO {

    @NotNull(message = "O tipo de entrega é obrigatório.")
    private TipoEntrega tipo;

    @NotBlank(message = "A URL do documento é obrigatória.")
    @Size(max = 2048, message = "A URL deve ter no máximo 2048 caracteres.")
    @URL(protocol = "https", message = "A URL do documento deve utilizar o protocolo https://.")
    private String urlDocumento;
}
