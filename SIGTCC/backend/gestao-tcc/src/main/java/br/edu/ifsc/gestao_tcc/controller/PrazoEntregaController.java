package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.dto.PrazoEntregaRequestDTO;
import br.edu.ifsc.gestao_tcc.dto.PrazoEntregaResponseDTO;
import br.edu.ifsc.gestao_tcc.service.PrazoEntregaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/prazos")
@RequiredArgsConstructor
public class PrazoEntregaController {

    private static final String CLAIM_EMAIL = "email";

    private final PrazoEntregaService prazoEntregaService;

    @PostMapping
    public ResponseEntity<PrazoEntregaResponseDTO> cadastraPrazo(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid PrazoEntregaRequestDTO dto) {

        PrazoEntregaResponseDTO prazo = prazoEntregaService.cadastrar(jwt.getClaimAsString(CLAIM_EMAIL), dto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(prazo.id())
                .toUri();

        return ResponseEntity.created(location).body(prazo);
    }
}
