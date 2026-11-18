package br.edu.ifsc.gestao_tcc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrientadorUpdateDTO(
        @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
        String nome,

        @Email(message = "Formato de e-mail inválido")
        @Size(max = 100, message = "O e-mail deve ter no máximo 100 caracteres.")
        String email,

        @Size(max = 100, message = "O departamento deve ter no máximo 100 caracteres")
        String departamento,

        @Size(min = 1, message = "Informe ao menos uma linha de pesquisa.")
        List<
                @NotBlank(message = "A linha de pesquisa não pode ficar em branco.")
                @Size(min = 2, max = 80, message = "A linha de pesquisa deve ter entre 2 e 80 caracteres.")
                String> linhasDePesquisa,

        @Min(value = 0, message = "O número de vagas não pode ser negativo")
        Integer vagasDisponiveis,

        @Size(max = 500, message = "A biografia deve ter no máximo 500 caracteres")
        String biografia
) {
}
