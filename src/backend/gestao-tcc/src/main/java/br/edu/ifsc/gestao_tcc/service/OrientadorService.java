package br.edu.ifsc.gestao_tcc.service;

import br.edu.ifsc.gestao_tcc.dto.OrientadorRequestDTO;
import br.edu.ifsc.gestao_tcc.exception.EmailDuplicadoException;
import br.edu.ifsc.gestao_tcc.model.LinhaPesquisa;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.PerfilOrientador;
import br.edu.ifsc.gestao_tcc.repository.OrientadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}