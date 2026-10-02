package br.edu.ifsc.gestao_tcc.observer;

import br.edu.ifsc.gestao_tcc.event.SolicitacaoStatusChangedEvent;
import br.edu.ifsc.gestao_tcc.model.PerfilOrientador;
import br.edu.ifsc.gestao_tcc.model.StatusSolicitacao;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AtualizadorVagasObserver {

    @EventListener
    public void atualizarVagas(SolicitacaoStatusChangedEvent event) {
        if (event.statusNovo() != StatusSolicitacao.ACEITA) {
            return;
        }

        PerfilOrientador perfil = event.solicitacao()
                .getOrientador()
                .getPerfil();

        perfil.setVagasDisponiveis(perfil.getVagasDisponiveis() - 1);
    }
}
