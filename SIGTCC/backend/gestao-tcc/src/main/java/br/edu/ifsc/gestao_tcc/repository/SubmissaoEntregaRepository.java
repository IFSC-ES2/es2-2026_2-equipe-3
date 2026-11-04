package br.edu.ifsc.gestao_tcc.repository;

import br.edu.ifsc.gestao_tcc.model.SubmissaoEntrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissaoEntregaRepository extends JpaRepository<SubmissaoEntrega, Long> {

    List<SubmissaoEntrega> findByAlunoIdOrderByEnviadoEmDesc(Long alunoId);
}