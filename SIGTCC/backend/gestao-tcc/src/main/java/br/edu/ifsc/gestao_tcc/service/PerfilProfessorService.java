package br.edu.ifsc.gestao_tcc.service;

import br.edu.ifsc.gestao_tcc.dto.PerfilProfessorResponseDTO;
import br.edu.ifsc.gestao_tcc.exception.ResourceNotFoundException;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.repository.OrientadorRepository;
import br.edu.ifsc.gestao_tcc.repository.TurmaTCCRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PerfilProfessorService {

    private final OrientadorRepository orientadorRepository;
    private final TurmaTCCRepository turmaTCCRepository;

    @Transactional(readOnly = true)
    public PerfilProfessorResponseDTO buscarPerfilPorEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResourceNotFoundException("Não foi possível identificar o professor autenticado.");
        }

        Orientador professor = orientadorRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Professor com e-mail " + email + " não foi encontrado."));

        boolean responsavelTurma = turmaTCCRepository
                .existsByProfessorResponsavelIdAndVigenteTrue(professor.getId());

        return new PerfilProfessorResponseDTO(
                professor.getId(),
                professor.getNome(),
                professor.getEmail(),
                professor.getDepartamento(),
                responsavelTurma
        );
    }
}
