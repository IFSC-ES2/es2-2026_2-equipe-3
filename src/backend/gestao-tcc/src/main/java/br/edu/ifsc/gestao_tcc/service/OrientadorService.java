package br.edu.ifsc.gestao_tcc.service;

import br.edu.ifsc.gestao_tcc.dto.OrientadorRequestDTO;
import br.edu.ifsc.gestao_tcc.exception.EmailDuplicadoException;
import br.edu.ifsc.gestao_tcc.dto.OrientadorResponseDTO;
import br.edu.ifsc.gestao_tcc.exception.ResourceNotFoundException;
import br.edu.ifsc.gestao_tcc.model.LinhaPesquisa;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.PerfilOrientador;
import br.edu.ifsc.gestao_tcc.repository.OrientadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrientadorService {

    private final OrientadorRepository orientadorRepository;

    @Transactional
    public Orientador cadastrar(OrientadorRequestDTO dto) {
        if (orientadorRepository.existsByEmail(dto.getEmail())) {
            throw new EmailDuplicadoException("Já existe um orientador cadastrado com o e-mail: " + dto.getEmail());
        }

        Orientador orientador = new Orientador();
        orientador.setNome(dto.getNome());
        orientador.setEmail(dto.getEmail());
        orientador.setDepartamento(dto.getDepartamento());

        PerfilOrientador perfil = PerfilOrientador.builder()
                .vagasDisponiveis(dto.getVagasDisponiveis())
                .biografia(dto.getBiografia())
                .orientador(orientador)
                .build();

        if (dto.getLinhasDePesquisa() != null) {
            List<LinhaPesquisa> linhas = dto.getLinhasDePesquisa().stream()
                    .map(nomeLinha -> {
                        LinhaPesquisa linha = new LinhaPesquisa();
                        linha.setNome(nomeLinha);
                        linha.setPerfilOrientador(perfil);
                        return linha;
                    }).toList();    
            perfil.setLinhasPesquisa(linhas);
        }

        orientador.setPerfil(perfil);

        return orientadorRepository.save(orientador);
    }
  
    @Transactional(readOnly = true)
    public OrientadorResponseDTO buscarPorId(Long id) {
        Orientador orientador = orientadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Orientador com identificador " + id + " não foi encontrado."));

        return toResponseDTO(orientador);
    }

    public OrientadorResponseDTO toResponseDTO(Orientador orientador) {
        PerfilOrientador perfil = orientador.getPerfil();
        List<String> linhasDePesquisa = Collections.emptyList();
        Integer vagasDisponiveis = 0;
        String biografia = null;

        if (perfil != null) {
            vagasDisponiveis = perfil.getVagasDisponiveis();
            biografia = perfil.getBiografia();
            if (perfil.getLinhasPesquisa() != null) {
                linhasDePesquisa = perfil.getLinhasPesquisa().stream()
                        .map(LinhaPesquisa::getNome)
                        .toList();
            }
        }

        return new OrientadorResponseDTO(
                orientador.getId(),
                orientador.getNome(),
                orientador.getEmail(),
                orientador.getDepartamento(),
                linhasDePesquisa,
                vagasDisponiveis,
                biografia,
                orientador.getAtivo() != null ? orientador.getAtivo() : true
        );
    }
}
