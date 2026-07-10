package com.pharmacr.controller;

import com.pharmacr.domain.EntradaInventario;
import com.pharmacr.domain.Medicamento;
import com.pharmacr.service.EntradaInventarioService;
import com.pharmacr.service.MedicamentoService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private final MedicamentoService medicamentoService;
    private final EntradaInventarioService entradaInventarioService;

    public ReporteController(MedicamentoService medicamentoService,
            EntradaInventarioService entradaInventarioService) {
        this.medicamentoService = medicamentoService;
        this.entradaInventarioService = entradaInventarioService;
    }

    // Reporte de inventario: existencias, bajo mínimo y próximos a vencer (HU-14, HU-15)
    @GetMapping("/inventario")
    public String inventario(@RequestParam(defaultValue = "90") Integer dias, Model model) {
        var medicamentos = medicamentoService.getMedicamentos(true);

        List<Medicamento> bajoMinimo = new ArrayList<>();
        for (Medicamento medicamento : medicamentos) {
            if (medicamento.getStockActual() < medicamento.getStockMinimo()) {
                bajoMinimo.add(medicamento);
            }
        }

        var fechaLimite = LocalDate.now().plusDays(dias);
        List<EntradaInventario> porVencer = new ArrayList<>();
        for (EntradaInventario entrada : entradaInventarioService.getEntradas()) {
            if (!entrada.getFechaVencimiento().isAfter(fechaLimite)) {
                porVencer.add(entrada);
            }
        }

        model.addAttribute("medicamentos", medicamentos);
        model.addAttribute("bajoMinimo", bajoMinimo);
        model.addAttribute("porVencer", porVencer);
        model.addAttribute("dias", dias);
        return "reportes/inventario";
    }
}
