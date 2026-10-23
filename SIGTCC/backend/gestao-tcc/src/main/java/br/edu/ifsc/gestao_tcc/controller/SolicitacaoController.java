package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.dto.AtualizaStatusRequest;
import br.edu.ifsc.gestao_tcc.dto.SolicitacaoRequestDTO;
import br.edu.ifsc.gestao_tcc.dto.SolicitacaoResponseDTO;
import br.edu.ifsc.gestao_tcc.service.SolicitacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/solicitacoes")
@RequiredArgsConstructor
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;

    @PostMapping
    public ResponseEntity<SolicitacaoResponseDTO> criarSolicitacao(@RequestBody @Valid SolicitacaoRequestDTO dto) {
        SolicitacaoResponseDTO resposta = solicitacaoService.criarSolicitacao(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(resposta.id())
                .toUri();
        return ResponseEntity.created(location).body(resposta);
    }

    @GetMapping("/orientador/{orientadorId}")
    public ResponseEntity<List<SolicitacaoResponseDTO>> listarPorOrientador(
            @PathVariable Long orientadorId,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(solicitacaoService.listarPorOrientador(orientadorId, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitacaoResponseDTO> buscarPorId(
            @PathVariable Long id
    )   {
        return ResponseEntity.ok(solicitacaoService.buscarPorId(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SolicitacaoResponseDTO> atualizarStatus(
            @PathVariable Long id,
            @RequestBody AtualizaStatusRequest request
    ) {
        return ResponseEntity.ok(solicitacaoService.atualizarStatus(id, request));
    }
}