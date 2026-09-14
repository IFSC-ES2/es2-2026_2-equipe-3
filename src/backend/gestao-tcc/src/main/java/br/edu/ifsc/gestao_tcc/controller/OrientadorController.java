package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.dto.OrientadorRequestDTO;
import br.edu.ifsc.gestao_tcc.dto.OrientadorResponseDTO;
import br.edu.ifsc.gestao_tcc.dto.OrientadorUpdateDTO;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.service.OrientadorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orientadores")
@RequiredArgsConstructor
public class OrientadorController {

    private final OrientadorService orientadorService;

    @PostMapping
    public ResponseEntity<Void> cadastraOrientador(@RequestBody @Valid OrientadorRequestDTO orientadorDTO) {
        Orientador orientadorCriado = orientadorService.cadastrar(orientadorDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(orientadorCriado.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public ResponseEntity<List<OrientadorResponseDTO>> listaOrientadores(@RequestParam(required = false) String area) {
        List<OrientadorResponseDTO> lista = orientadorService.listarOrientadores(area);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrientadorResponseDTO> ListaOrientadorID(@PathVariable Long id) {
        OrientadorResponseDTO orientadorResponse = orientadorService.buscarPorId(id);
        return ResponseEntity.ok(orientadorResponse);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrientadorResponseDTO> atualizaOrientadorID(
            @PathVariable Long id,
            @RequestBody @Valid OrientadorUpdateDTO orientadorDTO) {

        OrientadorResponseDTO orientadorResponse = orientadorService.atualizar(id, orientadorDTO);
        return ResponseEntity.ok(orientadorResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletaOrientadorID(@PathVariable Long id) {
        orientadorService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}