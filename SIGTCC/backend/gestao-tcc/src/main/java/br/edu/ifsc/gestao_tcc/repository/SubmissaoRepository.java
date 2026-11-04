package br.edu.ifsc.gestao_tcc.repository;

import br.edu.ifsc.gestao_tcc.model.Submissao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissaoRepository extends JpaRepository<Submissao, Long> {

    List<Submissao> findByAlunoIdOrderByEnviadoEmDesc(Long alunoId);
}
