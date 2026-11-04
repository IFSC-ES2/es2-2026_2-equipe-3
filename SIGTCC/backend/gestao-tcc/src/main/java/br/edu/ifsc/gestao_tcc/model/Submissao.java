package br.edu.ifsc.gestao_tcc.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Representa a submissão de uma entrega (Projeto de TCC, TCC 1 ou TCC 2) feita
 * pelo aluno por meio de um link externo, conforme a ADR-0013.
 * <p>
 * Nenhum binário é recebido ou armazenado pelo backend: apenas a URL informada
 * pelo aluno é persistida junto com os metadados de rastreabilidade da entrega.
 */
@Entity
@Table(name = "submissoes")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Submissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private TipoEntrega tipo;

    @Column(name = "url_documento", length = 2048, nullable = false)
    private String urlDocumento;

    @CreationTimestamp
    @Column(name = "enviado_em", nullable = false, updatable = false)
    private Instant enviadoEm;
}
