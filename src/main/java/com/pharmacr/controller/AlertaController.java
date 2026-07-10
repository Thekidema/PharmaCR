package com.pharmacr.controller;

import com.pharmacr.service.AlertaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/alerta")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    // Alertas de stock minimo visibles para admin y encargado HU-16
    @GetMapping("/listado")
    public String listado(Model model) {
        var alertas = alertaService.getAlertas(true);
        model.addAttribute("alertas", alertas);
        model.addAttribute("totalAlertas", alertas.size());
        return "alerta/listado";
    }
}
