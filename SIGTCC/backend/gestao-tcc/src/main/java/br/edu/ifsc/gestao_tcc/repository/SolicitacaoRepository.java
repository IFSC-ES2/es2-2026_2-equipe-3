package br.edu.ifsc.gestao_tcc.repository;

import br.edu.ifsc.gestao_tcc.model.SolicitacaoOrientacao;
import br.edu.ifsc.gestao_tcc.model.StatusSolicitacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitacaoRepository extends JpaRepository<SolicitacaoOrientacao, Long> {

    List<SolicitacaoOrientacao> findByOrientadorIdOrderByCriadoEmDesc(Long orientadorId);

    List<SolicitacaoOrientacao> findByOrientadorIdAndStatusOrderByCriadoEmDesc(
            Long orientadorId,
            StatusSolicitacao status
    );
}
