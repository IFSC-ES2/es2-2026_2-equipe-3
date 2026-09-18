package br.edu.ifsc.gestao_tcc.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "linhas_pesquisa")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinhaPesquisa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 80)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "perfil_id")
    private PerfilOrientador perfilOrientador;
}
