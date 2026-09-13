package br.edu.ifsc.gestao_tcc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrientadorRequestDTO {

    @NotBlank(message = "O nome é obrigatório.")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Formato de e-mail inválido.")
    private String email;

    @NotNull(message = "O número de vagas é obrigatório.")
    @Min(value = 1, message = "O orientador deve ter no mínimo 1 vaga.")
    private Integer vagas;
}