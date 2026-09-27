package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.dto.AtualizaStatusRequest;
import br.edu.ifsc.gestao_tcc.dto.SolicitacaoResponse;
import br.edu.ifsc.gestao_tcc.service.SolicitacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/solicitacoes")
@RequiredArgsConstructor
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;

    @GetMapping("/orientador/{orientadorId}")
    public ResponseEntity<List<SolicitacaoResponse>> listarPorOrientador(
            @PathVariable Long orientadorId,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(
                solicitacaoService.listarPorOrientador(orientadorId, status)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SolicitacaoResponse> atualizarStatus(
            @PathVariable Long id,
            @RequestBody AtualizaStatusRequest request
    ) {
        return ResponseEntity.ok(
                solicitacaoService.atualizarStatus(id, request)
        );
    }
}
