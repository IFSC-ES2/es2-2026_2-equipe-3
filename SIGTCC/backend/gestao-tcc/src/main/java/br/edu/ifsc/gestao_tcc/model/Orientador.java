package br.edu.ifsc.gestao_tcc.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "orientadores")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Orientador extends Usuario {

    @Column(length = 100)
    private String departamento;

    @Builder.Default
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @OneToOne(mappedBy = "orientador", cascade = CascadeType.ALL)
    private PerfilOrientador perfil;
}
