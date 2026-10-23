package br.edu.ifsc.gestao_tcc.repository;

import br.edu.ifsc.gestao_tcc.model.PrazoEntrega;
import br.edu.ifsc.gestao_tcc.model.TipoEntrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrazoEntregaRepository extends JpaRepository<PrazoEntrega, Long> {

    List<PrazoEntrega> findByTurmaId(Long turmaId);

    boolean existsByTurmaIdAndTipo(Long turmaId, TipoEntrega tipo);
}
