package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.dto.PerfilProfessorResponseDTO;
import br.edu.ifsc.gestao_tcc.service.PerfilProfessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/professores")
@RequiredArgsConstructor
public class PerfilProfessorController {

    private static final String CLAIM_EMAIL = "email";

    private final PerfilProfessorService perfilProfessorService;

    @GetMapping("/me")
    public ResponseEntity<PerfilProfessorResponseDTO> buscaPerfilProfessorAutenticado(
            @AuthenticationPrincipal Jwt jwt) {
        PerfilProfessorResponseDTO perfil = perfilProfessorService
                .buscarPerfilPorEmail(jwt.getClaimAsString(CLAIM_EMAIL));
        return ResponseEntity.ok(perfil);
    }
}
