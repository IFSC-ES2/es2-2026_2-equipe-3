package br.edu.ifsc.gestao_tcc.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "entregas",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_entrega_aluno_turma_tipo",
                columnNames = {"aluno_id", "turma_id", "tipo"}
        )
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "orientador_id", nullable = false)
    private Orientador orientador;

    @ManyToOne(optional = false)
    @JoinColumn(name = "turma_id", nullable = false)
    private TurmaTCC turma;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private TipoEntrega tipo;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private StatusEntrega status;

    @Column(name = "url_documento", length = 2048, nullable = false)
    private String urlDocumento;

    @Builder.Default
    @Column(nullable = false)
    private Boolean atrasada = false;

    @Column(name = "enviado_em", nullable = false)
    private Instant enviadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private Instant atualizadoEm;
}

