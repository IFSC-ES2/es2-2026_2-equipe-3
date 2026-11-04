package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.dto.SubmissaoRequestDTO;
import br.edu.ifsc.gestao_tcc.dto.SubmissaoResponseDTO;
import br.edu.ifsc.gestao_tcc.service.SubmissaoService;
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

/**
 * Endpoint de submissão de entregas por link externo (US06).
 * <p>
 * Conforme a ADR-0013, a requisição é um JSON simples ({@code POST /api/v1/submissoes})
 * contendo apenas a URL do documento/repositório; não há suporte a multipart/upload.
 * O protocolo HTTPS é exigido pela validação do {@link SubmissaoRequestDTO}.
 */
@RestController
@RequestMapping("/api/v1/submissoes")
@RequiredArgsConstructor
public class SubmissaoController {

    private static final String CLAIM_EMAIL = "email";

    private final SubmissaoService submissaoService;

    @PostMapping
    public ResponseEntity<SubmissaoResponseDTO> criarSubmissao(
            @RequestBody @Valid SubmissaoRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt) {

        SubmissaoResponseDTO resposta = submissaoService.criarSubmissao(dto, jwt.getClaimAsString(CLAIM_EMAIL));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(resposta.id())
                .toUri();

        return ResponseEntity.created(location).body(resposta);
    }
}
