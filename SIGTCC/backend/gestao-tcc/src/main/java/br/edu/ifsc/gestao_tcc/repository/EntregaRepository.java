package br.edu.ifsc.gestao_tcc.repository;

import br.edu.ifsc.gestao_tcc.model.Entrega;
import br.edu.ifsc.gestao_tcc.model.StatusEntrega;
import br.edu.ifsc.gestao_tcc.model.TipoEntrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {

    Optional<Entrega> findByAlunoIdAndTurmaIdAndTipo(Long alunoId, Long turmaId, TipoEntrega tipo);

    boolean existsByAlunoIdAndTurmaIdAndTipo(Long alunoId, Long turmaId, TipoEntrega tipo);

    List<Entrega> findByAlunoIdOrderByTipoAsc(Long alunoId);

    List<Entrega> findByOrientadorIdOrderByEnviadoEmDesc(Long orientadorId);

    List<Entrega> findByOrientadorIdAndStatusOrderByEnviadoEmDesc(Long orientadorId, StatusEntrega status);

    List<Entrega> findByTurmaId(Long turmaId);
}

