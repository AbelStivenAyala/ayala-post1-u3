package com.universidad.confudes.certificados;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/certificados")
public class ControladorCertificados {
    private final ServicioEmisionCertificados servicioEmisionCertificados;

    public ControladorCertificados(ServicioEmisionCertificados servicioEmisionCertificados) {
        this.servicioEmisionCertificados = servicioEmisionCertificados;
    }

    @PostMapping("/{eventoId}/{participanteId}")
    public ResponseEntity<String> emitir(@PathVariable String eventoId, @PathVariable String participanteId,
                                          @RequestParam String nombre, @RequestParam String correoDestino) {
        try {
            String resultado = servicioEmisionCertificados.emitir(eventoId, participanteId, nombre, correoDestino);
            return ResponseEntity.ok(resultado);
        } catch (AsistenciaInsuficienteException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}