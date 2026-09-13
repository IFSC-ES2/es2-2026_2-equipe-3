package br.edu.ifsc.gestao_tcc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class OrientadorRequestDTO {

    @NotBlank(message = "O nome é obrigatório.")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Formato de e-mail inválido.")
    private String email;

    private String departamento;

    private String biografia;

    @NotEmpty(message = "Informe ao menos uma linha de pesquisa.")
    private List<String> linhasDePesquisa;

    @NotNull(message = "O número de vagas é obrigatório.")
    @Min(value = 0, message = "O orientador deve ter no mínimo 0 vagas.")
    private Integer vagasDisponiveis;
}