package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.dto.OrientadorRequestDTO;
import br.edu.ifsc.gestao_tcc.dto.OrientadorUpdateDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orientadores")
public class OrientadorController {

    @PostMapping
    public ResponseEntity<Void> cadastraOrientador(@RequestBody @Valid OrientadorRequestDTO orientadorDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<Void> listaOrientadores(@RequestParam(required = false) String area) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Void> ListaOrientadorID(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> atualizaOrientadorID(
            @PathVariable Long id,
            @RequestBody @Valid OrientadorUpdateDTO orientadorDTO) {

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletaOrientadorID(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}
