package br.edu.ifsc.gestao_tcc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

// Os limites de tamanho seguem as colunas do banco (V1) e a validação do frontend.
@Data
public class OrientadorRequestDTO {

    @NotBlank(message = "O nome é obrigatório.")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Formato de e-mail inválido.")
    @Size(max = 100, message = "O e-mail deve ter no máximo 100 caracteres.")
    private String email;

    @Size(max = 100, message = "O departamento deve ter no máximo 100 caracteres.")
    private String departamento;

    @Size(max = 500, message = "A biografia deve ter no máximo 500 caracteres.")
    private String biografia;

    @NotEmpty(message = "Informe ao menos uma linha de pesquisa.")
    private List<
            @NotBlank(message = "A linha de pesquisa não pode ficar em branco.")
            @Size(min = 2, max = 80, message = "A linha de pesquisa deve ter entre 2 e 80 caracteres.")
            String> linhasDePesquisa;

    @NotNull(message = "O número de vagas é obrigatório.")
    @Min(value = 0, message = "O orientador deve ter no mínimo 0 vagas.")
    private Integer vagasDisponiveis;
}
