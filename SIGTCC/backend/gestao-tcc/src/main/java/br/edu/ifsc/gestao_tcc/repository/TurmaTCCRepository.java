package br.edu.ifsc.gestao_tcc.repository;

import br.edu.ifsc.gestao_tcc.model.TurmaTCC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TurmaTCCRepository extends JpaRepository<TurmaTCC, Long> {

    /**
     * Verifica se o professor informado é o responsável pela turma de TCC vigente.
     *
     * @param professorId Identificador do professor/orientador.
     * @return true se o professor for o responsável pela turma vigente, false caso contrário.
     */
    boolean existsByProfessorResponsavelIdAndVigenteTrue(Long professorId);

    /**
     * Busca a turma de TCC atualmente vigente no sistema.
     *
     * @return Optional contendo a turma vigente, se houver.
     */
    Optional<TurmaTCC> findByVigenteTrue();

    /**
     * Busca a turma de TCC pelo identificador textual do semestre (ex: "2026/2").
     *
     * @param semestre Identificador do semestre letivo.
     * @return Optional contendo a turma correspondente, se houver.
     */
    Optional<TurmaTCC> findBySemestre(String semestre);

    /**
     * Lista todas as turmas de TCC sob a responsabilidade de um professor.
     *
     * @param professorId Identificador do professor/orientador.
     * @return Lista de turmas associadas ao professor.
     */
    List<TurmaTCC> findByProfessorResponsavelId(Long professorId);
}
