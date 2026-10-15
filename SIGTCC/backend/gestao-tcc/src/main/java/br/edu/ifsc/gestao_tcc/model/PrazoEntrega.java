package br.edu.ifsc.gestao_tcc.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "prazos_entrega",
        uniqueConstraints = @UniqueConstraint(name = "uk_prazo_turma_tipo", columnNames = {"turma_id", "tipo"})
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrazoEntrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "turma_id", nullable = false)
    private TurmaTCC turma;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private TipoEntrega tipo;

    @Column(name = "data_limite", nullable = false)
    private LocalDate dataLimite;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private Instant atualizadoEm;
}
