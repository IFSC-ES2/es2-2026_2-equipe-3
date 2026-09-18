package br.edu.ifsc.gestao_tcc.repository;

import br.edu.ifsc.gestao_tcc.model.Orientador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrientadorRepository extends JpaRepository<Orientador, Long> {
    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT o FROM Orientador o JOIN o.perfil p JOIN p.linhasPesquisa lp WHERE LOWER(lp.nome) LIKE LOWER(CONCAT('%', :area, '%'))")
    List<Orientador> findByLinhasPesquisaNomeContainingIgnoreCase(@Param("area") String area);
}