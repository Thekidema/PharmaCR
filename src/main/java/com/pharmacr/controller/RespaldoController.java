package com.pharmacr.controller;

import com.pharmacr.service.RespaldoService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


 //HU-19: permite al administrador generar y descargar un respaldo de la base de datos

@Controller
@RequestMapping("/respaldo")
@PreAuthorize("hasRole('ADMIN')")
public class RespaldoController {

    private static final DateTimeFormatter SELLO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    private final RespaldoService respaldoService;

    public RespaldoController(RespaldoService respaldoService) {
        this.respaldoService = respaldoService;
    }

    @GetMapping("/generar")
    public String generar(Model model) {
        model.addAttribute("nombreBaseDatos", respaldoService.getNombreBaseDatos());
        return "/respaldo/generar";
    }

    @GetMapping("/descargar")
    public ResponseEntity<Resource> descargar() {
        var contenido = respaldoService.generarRespaldo();
        var nombre = "respaldo_pharmacr_" + LocalDateTime.now().format(SELLO) + ".sql";

        Resource archivo = new ByteArrayResource(contenido.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .contentType(MediaType.parseMediaType("application/sql"))
                .body(archivo);
    }
}
