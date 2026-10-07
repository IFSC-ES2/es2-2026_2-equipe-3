package br.edu.ifsc.gestao_tcc.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "turmas_tcc")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TurmaTCC {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false)
    private String semestre;

    @Builder.Default
    @Column(nullable = false)
    private Boolean vigente = false;

    @ManyToOne(optional = false)
    @JoinColumn(name = "professor_responsavel_id", nullable = false)
    private Orientador professorResponsavel;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;
}
