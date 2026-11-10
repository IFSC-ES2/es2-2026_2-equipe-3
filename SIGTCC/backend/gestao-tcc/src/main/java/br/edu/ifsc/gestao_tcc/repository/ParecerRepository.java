package br.edu.ifsc.gestao_tcc.repository;

import br.edu.ifsc.gestao_tcc.model.Parecer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParecerRepository extends JpaRepository<Parecer, Long> {

    Optional<Parecer> findFirstByEntregaIdOrderByRegistradoEmDescIdDesc(Long entregaId);

    Optional<Parecer> findFirstByEntregaIdOrderByRegistradoEmDesc(Long entregaId);

    List<Parecer> findByEntregaIdOrderByRegistradoEmDesc(Long entregaId);
}

