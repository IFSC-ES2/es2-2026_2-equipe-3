package br.edu.ifsc.gestao_tcc.observer;

import br.edu.ifsc.gestao_tcc.event.SolicitacaoStatusChangedEvent;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.PerfilOrientador;
import br.edu.ifsc.gestao_tcc.model.SolicitacaoOrientacao;
import br.edu.ifsc.gestao_tcc.model.StatusSolicitacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AtualizadorVagasObserverTest {

    private AtualizadorVagasObserver observer;

    @BeforeEach
    void setUp() {
        observer = new AtualizadorVagasObserver();
    }

    @Test
    @DisplayName("Deve decrementar o número de vagas quando o novo status da solicitação for ACEITA")
    void atualizarVagas_QuandoStatusAceita_DeveDecrementarVagas() {
        PerfilOrientador perfil = new PerfilOrientador();
        perfil.setVagasDisponiveis(3);

        Orientador orientador = new Orientador();
        orientador.setPerfil(perfil);

        SolicitacaoOrientacao solicitacao = new SolicitacaoOrientacao();
        solicitacao.setOrientador(orientador);

        SolicitacaoStatusChangedEvent event = new SolicitacaoStatusChangedEvent(
                solicitacao,
                StatusSolicitacao.PENDENTE,
                StatusSolicitacao.ACEITA
        );

        observer.atualizarVagas(event);

        assertEquals(2, perfil.getVagasDisponiveis());
    }

    @Test
    @DisplayName("Não deve alterar o número de vagas quando o novo status for diferente de ACEITA (ex: RECUSADA)")
    void atualizarVagas_QuandoStatusNaoForAceita_NaoDeveAlterarVagas() {
        PerfilOrientador perfil = new PerfilOrientador();
        perfil.setVagasDisponiveis(3);

        Orientador orientador = new Orientador();
        orientador.setPerfil(perfil);

        SolicitacaoOrientacao solicitacao = new SolicitacaoOrientacao();
        solicitacao.setOrientador(orientador);

        SolicitacaoStatusChangedEvent event = new SolicitacaoStatusChangedEvent(
                solicitacao,
                StatusSolicitacao.PENDENTE,
                StatusSolicitacao.RECUSADA
        );

        observer.atualizarVagas(event);

        assertEquals(3, perfil.getVagasDisponiveis());
    }
}