package br.edu.ifsc.gestao_tcc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrientadorUpdateDTO(
        @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
        String nome,

        @Email(message = "Formato de e-mail inválido")
        String email,

        @Size(max = 100, message = "O departamento deve ter no máximo 100 caracteres")
        String departamento,

        List<String> linhasDePesquisa,

        @Min(value = 0, message = "O número de vagas não pode ser negativo")
        Integer vagasDisponiveis,

        @Size(max = 500, message = "A biografia deve ter no máximo 500 caracteres")
        String biografia
) {
}

