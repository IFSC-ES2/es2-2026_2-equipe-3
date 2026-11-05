package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.dto.EntregaRequestDTO;
import br.edu.ifsc.gestao_tcc.dto.EntregaResponseDTO;
import br.edu.ifsc.gestao_tcc.dto.SubmissaoEntregaResultado;
import br.edu.ifsc.gestao_tcc.service.EntregaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/entregas")
@RequiredArgsConstructor
public class EntregaController {

    private static final String CLAIM_EMAIL = "email";

    private final EntregaService entregaService;

    @PostMapping
    public ResponseEntity<EntregaResponseDTO> submeterEntrega(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid EntregaRequestDTO dto) {

        SubmissaoEntregaResultado resultado = entregaService.submeter(jwt.getClaimAsString(CLAIM_EMAIL), dto);

        if (!resultado.criada()) {
            return ResponseEntity.ok(resultado.entrega());
        }

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(resultado.entrega().id())
                .toUri();

        return ResponseEntity.created(location).body(resultado.entrega());
    }
}
