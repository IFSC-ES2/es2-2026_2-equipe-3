package br.edu.ifsc.gestao_tcc.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "submissoes_entrega")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmissaoEntrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private TipoEntrega tipo;

    @Column(length = 2048, nullable = false)
    private String url;

    @CreationTimestamp
    @Column(name = "enviado_em", nullable = false, updatable = false)
    private Instant enviadoEm;
}