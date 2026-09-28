package br.edu.ifsc.gestao_tcc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SolicitacaoRequestDTO {

    @NotNull(message = "O ID do orientador é obrigatório.")
    private Long orientadorId;

    @NotNull(message = "O ID do aluno é obrigatório.")
    private Long alunoId;

    @NotBlank(message = "O tema é obrigatório.")
    @Size(min = 5, max = 150, message = "O tema deve ter entre 5 e 150 caracteres.")
    private String tema;

    @NotBlank(message = "A mensagem é obrigatória.")
    @Size(max = 1000, message = "A mensagem deve ter no máximo 1000 caracteres.")
    private String mensagem;
}