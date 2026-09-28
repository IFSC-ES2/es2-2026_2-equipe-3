package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.dto.SolicitacaoRequestDTO;
import br.edu.ifsc.gestao_tcc.dto.SolicitacaoResponseDTO;
import br.edu.ifsc.gestao_tcc.service.SolicitacaoOrientacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/solicitacoes")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, exposedHeaders = "Location")
@RequiredArgsConstructor
public class SolicitacaoOrientacaoController {

    private final SolicitacaoOrientacaoService solicitacaoService;

    @PostMapping
    public ResponseEntity<Void> criarSolicitacao(@RequestBody @Valid SolicitacaoRequestDTO dto) {
        SolicitacaoResponseDTO resposta = solicitacaoService.criarSolicitacao(dto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(resposta.id())
                .toUri();

        return ResponseEntity.created(location).build();
    }
}