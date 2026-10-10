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

    @Query("""
        SELECT DISTINCT o
        FROM Orientador o
        JOIN o.perfil p
        LEFT JOIN p.linhasPesquisa lp
        WHERE (:area IS NULL OR LOWER(lp.nome) LIKE LOWER(CONCAT('%', :area, '%')))
          AND (:temVagas IS NULL OR :temVagas = false OR p.vagasDisponiveis > 0)
    """)
    List<Orientador> findByFiltros(@Param("area") String area, @Param("temVagas") Boolean temVagas);

}
