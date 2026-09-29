package br.edu.ifsc.gestao_tcc.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SolicitacaoRequestDTO {

    @NotNull(message = "O ID do orientador é obrigatório.")
    private Long orientadorId;

    @NotNull(message = "Os dados do aluno são obrigatórios.")
    @Valid
    private AlunoDTO aluno;

    @NotBlank(message = "O tema é obrigatório.")
    @Size(min = 5, max = 150, message = "O tema deve ter entre 5 e 150 caracteres.")
    private String tema;

    @NotBlank(message = "A mensagem é obrigatória.")
    @Size(max = 1000, message = "A mensagem deve ter no máximo 1000 caracteres.")
    private String mensagem;

    @Data
    public static class AlunoDTO {
        @NotBlank(message = "O nome do aluno é obrigatório.")
        private String nome;

        @NotBlank(message = "O e-mail do aluno é obrigatório.")
        @Email(message = "Formato de e-mail inválido.")
        private String email;

        @NotBlank(message = "O curso do aluno é obrigatório.")
        private String curso;
    }
}