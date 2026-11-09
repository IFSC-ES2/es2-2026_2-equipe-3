package br.edu.ifsc.gestao_tcc.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "pareceres")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Parecer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "entrega_id", nullable = false)
    private Entrega entrega;

    @ManyToOne(optional = false)
    @JoinColumn(name = "orientador_id", nullable = false)
    private Orientador orientador;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private StatusEntrega resultado;

    @Column(length = 2000)
    private String comentario;

    @Column(name = "registrado_em", nullable = false)
    private Instant registradoEm;

    @PrePersist
    void prePersist() {
        if (this.registradoEm == null) {
            this.registradoEm = Instant.now();
        }
    }
}

