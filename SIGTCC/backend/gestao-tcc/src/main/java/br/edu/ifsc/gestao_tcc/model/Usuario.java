package br.edu.ifsc.gestao_tcc.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    protected String nome;

    @Column(length = 100, nullable = false, unique = true)
    protected String email;

    @Column(length = 45)
    protected String senha;

    @Column(length = 45)
    protected String matricula;

}
