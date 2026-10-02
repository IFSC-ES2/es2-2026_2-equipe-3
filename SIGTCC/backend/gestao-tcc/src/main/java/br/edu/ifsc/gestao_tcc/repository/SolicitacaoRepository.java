package br.edu.ifsc.gestao_tcc.repository;

import br.edu.ifsc.gestao_tcc.model.SolicitacaoOrientacao;
import br.edu.ifsc.gestao_tcc.model.StatusSolicitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitacaoRepository extends JpaRepository<SolicitacaoOrientacao, Long> {
    
    boolean existsByAlunoIdAndOrientadorIdAndStatus(Long alunoId, Long orientadorId, StatusSolicitacao status);
    
    List<SolicitacaoOrientacao> findByOrientadorIdOrderByCriadoEmDesc(Long orientadorId);
    
    List<SolicitacaoOrientacao> findByOrientadorIdAndStatusOrderByCriadoEmDesc(Long orientadorId, StatusSolicitacao status);
}