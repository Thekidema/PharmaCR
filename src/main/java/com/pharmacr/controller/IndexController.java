package com.pharmacr.controller;

import com.pharmacr.service.AlertaService;
import com.pharmacr.service.ConstanteService;
import com.pharmacr.service.MedicamentoService;
import com.pharmacr.service.VentaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    //Dias que se consideran proximo a vencer
    private static final int DIAS_POR_VENCER_DEFECTO = 30;

    private final MedicamentoService medicamentoService;
    private final AlertaService alertaService;
    private final VentaService ventaService;
    private final ConstanteService constanteService;

    public IndexController(MedicamentoService medicamentoService, AlertaService alertaService,
            VentaService ventaService, ConstanteService constanteService) {
        this.medicamentoService = medicamentoService;
        this.alertaService = alertaService;
        this.ventaService = ventaService;
        this.constanteService = constanteService;
    }

    //HU-20: panel principal con ventas del dia, alertas y medicamentos proximos a vencer
    @GetMapping("/")
    public String cargarIndex(Model model) {
        var alertas = alertaService.getAlertas(true);
        //El periodo se toma de la tabla constante, no esta quemado en el codigo
        var diasPorVencer = constanteService.valorEntero("dias.alerta.vencimiento", DIAS_POR_VENCER_DEFECTO);
        var porVencer = medicamentoService.getLotesPorVencer(diasPorVencer);

        //Los totales se resuelven con consultas agregadas, no trayendo todas las ventas
        model.addAttribute("totalMedicamentos", medicamentoService.contarActivos());
        model.addAttribute("totalAlertas", alertas.size());
        model.addAttribute("totalVentasDelDia", ventaService.contarVentasDelDia());
        model.addAttribute("montoVentasDelDia", ventaService.totalVentasDelDia());
        model.addAttribute("alertas", alertas);
        model.addAttribute("porVencer", porVencer);
        model.addAttribute("totalPorVencer", porVencer.size());
        model.addAttribute("diasPorVencer", diasPorVencer);
        return "/index";
    }
}
