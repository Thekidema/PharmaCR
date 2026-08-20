package com.pharmacr.controller;

import com.pharmacr.service.InventarioMovimientoService;
import com.pharmacr.service.MedicamentoService;
import com.pharmacr.service.UsuarioService;
import com.pharmacr.service.VentaService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/reportes")
@PreAuthorize("hasRole('ADMIN')")
public class ReporteController {

    private final MedicamentoService medicamentoService;
    private final VentaService ventaService;
    private final InventarioMovimientoService movimientoService;
    private final UsuarioService usuarioService;

    public ReporteController(MedicamentoService medicamentoService, VentaService ventaService,
            InventarioMovimientoService movimientoService, UsuarioService usuarioService) {
        this.medicamentoService = medicamentoService;
        this.ventaService = ventaService;
        this.movimientoService = movimientoService;
        this.usuarioService = usuarioService;
    }                                              // Reporte de inventario: existencias, bajo minimo y proximos a vencer (HU-14, HU-15)
    @GetMapping("/inventario")
    public String inventario(@RequestParam(defaultValue = "90") Integer dias, Model model) {
        model.addAttribute("medicamentos", medicamentoService.getMedicamentos(true));
        model.addAttribute("bajoMinimo", medicamentoService.getBajoMinimo());
        //Se acota entre hoy y el limite: los lotes ya vencidos no son "proximos a vencer"
        model.addAttribute("porVencer", medicamentoService.getLotesPorVencer(dias));
        model.addAttribute("dias", dias);
        return "/reportes/inventario";
    }

 
    @GetMapping("/ventas")
    public String ventas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model model) {

        var fin = (hasta == null) ? LocalDate.now() : hasta;
        var inicio = (desde == null) ? fin.minusDays(30) : desde;
        if (inicio.isAfter(fin)) {
                                                 //HU-13 Si el usuario invierte las fechas se corrigen en vez de devolver una lista vacia
            var intercambio = inicio;
            inicio = fin;
            fin = intercambio;
        }

        //El rango es inclusivo en ambos extremos: se busca hasta el final del dia  FIN
        var desdeHora = inicio.atStartOfDay();
        var hastaHora = fin.plusDays(1).atStartOfDay();

        model.addAttribute("desde", inicio);
        model.addAttribute("hasta", fin);
        model.addAttribute("ventas", ventaService.getVentasPorRango(desdeHora, hastaHora));
        model.addAttribute("totalVendido", ventaService.getTotalPorRango(desdeHora, hastaHora));
        model.addAttribute("transacciones", ventaService.contarPorRango(desdeHora, hastaHora));
        model.addAttribute("masVendidos", ventaService.getMasVendidos(desdeHora, hastaHora));
        return "/reportes/ventas";
    }

   //HU-17
    @GetMapping("/movimientos")
    public String movimientos(
            @RequestParam(required = false) Integer idMedicamento,
            @RequestParam(required = false) Integer idUsuario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model model) {

        var desdeHora = (desde == null) ? null : desde.atStartOfDay();
        var hastaHora = (hasta == null) ? null : hasta.plusDays(1).atStartOfDay();

        var movimientos = movimientoService.buscar(idMedicamento, idUsuario, desdeHora, hastaHora);

        model.addAttribute("movimientos", movimientos);
        model.addAttribute("totalMovimientos", movimientos.size());
        model.addAttribute("medicamentos", medicamentoService.getMedicamentos(true));
        model.addAttribute("usuarios", usuarioService.getUsuarios(true));
        model.addAttribute("idMedicamento", idMedicamento);
        model.addAttribute("idUsuario", idUsuario);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        return "/reportes/movimientos";
    }
}
