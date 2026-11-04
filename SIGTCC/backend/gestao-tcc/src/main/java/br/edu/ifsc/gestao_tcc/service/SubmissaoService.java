package br.edu.ifsc.gestao_tcc.service;

import br.edu.ifsc.gestao_tcc.dto.SubmissaoRequestDTO;
import br.edu.ifsc.gestao_tcc.dto.SubmissaoResponseDTO;
import br.edu.ifsc.gestao_tcc.exception.ResourceNotFoundException;
import br.edu.ifsc.gestao_tcc.model.Aluno;
import br.edu.ifsc.gestao_tcc.model.Submissao;
import br.edu.ifsc.gestao_tcc.repository.AlunoRepository;
import br.edu.ifsc.gestao_tcc.repository.SubmissaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negócio da submissão de entregas por link externo (US06 / ADR-0013).
 * <p>
 * A validação de protocolo HTTPS da URL é delegada ao Bean Validation
 * (ver {@link SubmissaoRequestDTO}), executada antes mesmo deste serviço ser
 * invocado pelo controller. Nenhum arquivo é lido, armazenado ou processado aqui.
 */
@Service
@RequiredArgsConstructor
public class SubmissaoService {

    private final SubmissaoRepository submissaoRepository;
    private final AlunoRepository alunoRepository;

    @Transactional
    public SubmissaoResponseDTO criarSubmissao(SubmissaoRequestDTO dto, String emailAluno) {
        Aluno aluno = alunoRepository.findByEmail(emailAluno)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado para o usuário autenticado."));

        Submissao submissao = Submissao.builder()
                .aluno(aluno)
                .tipo(dto.getTipo())
                .urlDocumento(dto.getUrlDocumento())
                .build();

        return SubmissaoResponseDTO.fromEntity(submissaoRepository.save(submissao));
    }
}
