package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.dto.OrientadorRequestDTO;
import br.edu.ifsc.gestao_tcc.dto.OrientadorResponseDTO;
import br.edu.ifsc.gestao_tcc.dto.OrientadorUpdateDTO;
import br.edu.ifsc.gestao_tcc.service.OrientadorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orientadores")
@RequiredArgsConstructor
public class OrientadorController {

    private final OrientadorService orientadorService;

    @PostMapping
    public ResponseEntity<Void> cadastraOrientador(@RequestBody @Valid OrientadorRequestDTO orientadorDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<Void> listaOrientadores(@RequestParam(required = false) String area) {
        return ResponseEntity.ok().build();
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
        return ResponseEntity.noContent().build();
    }
}
