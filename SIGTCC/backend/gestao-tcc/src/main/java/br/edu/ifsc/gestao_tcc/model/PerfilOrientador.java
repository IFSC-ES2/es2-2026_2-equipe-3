package br.edu.ifsc.gestao_tcc.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "perfis_professores")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfilOrientador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vagas_disponiveis", nullable = false)
    private int vagasDisponiveis;

    @Column(length = 500)
    private String biografia;

    @OneToOne
    @JoinColumn(name = "orientador_id")
    private Orientador orientador;

    @OneToMany(mappedBy = "perfilOrientador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LinhaPesquisa> linhasPesquisa;
}
