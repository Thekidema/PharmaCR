package com.pharmacr.controller;

import com.pharmacr.domain.Venta;
import com.pharmacr.service.AlertaService;
import com.pharmacr.service.MedicamentoService;
import com.pharmacr.service.VentaService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    private final MedicamentoService medicamentoService;
    private final AlertaService alertaService;
    private final VentaService ventaService;

    public IndexController(MedicamentoService medicamentoService, AlertaService alertaService,
            VentaService ventaService) {
        this.medicamentoService = medicamentoService;
        this.alertaService = alertaService;
        this.ventaService = ventaService;
    }

    @GetMapping("/")
    public String cargarIndex(Model model) {
        var medicamentos = medicamentoService.getMedicamentos(true);
        var alertas = alertaService.getAlertas(true);

        List<Venta> ventasDelDia = new ArrayList<>();
        var totalDelDia = BigDecimal.ZERO;
        for (Venta venta : ventaService.getVentas()) {
            if (venta.getFecha() != null && venta.getFecha().toLocalDate().equals(LocalDate.now())) {
                ventasDelDia.add(venta);
                totalDelDia = totalDelDia.add(venta.getTotal());
            }
        }

        model.addAttribute("totalMedicamentos", medicamentos.size());
        model.addAttribute("totalAlertas", alertas.size());
        model.addAttribute("totalVentasDelDia", ventasDelDia.size());
        model.addAttribute("montoVentasDelDia", totalDelDia);
        model.addAttribute("alertas", alertas);
        return "index";
    }
}
