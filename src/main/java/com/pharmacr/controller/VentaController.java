package com.pharmacr.controller;

import com.pharmacr.service.MedicamentoService;
import com.pharmacr.service.UsuarioService;
import com.pharmacr.service.VentaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/venta")
public class VentaController {

    private final VentaService ventaService;
    private final MedicamentoService medicamentoService;
    private final UsuarioService usuarioService;

    public VentaController(VentaService ventaService, MedicamentoService medicamentoService,
            UsuarioService usuarioService) {
        this.ventaService = ventaService;
        this.medicamentoService = medicamentoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/listado")
    public String listado(Model model) {
        var ventas = ventaService.getVentas();
        model.addAttribute("ventas", ventas);
        model.addAttribute("totalVentas", ventas.size());
        return "venta/listado";
    }

    @GetMapping("/detalle/{idVenta}")
    public String detalle(@PathVariable Integer idVenta, Model model) {
        var venta = ventaService.getVenta(idVenta);
        if (venta.isPresent()) {
            model.addAttribute("venta", venta.get());
            model.addAttribute("detalles", ventaService.getDetalles(venta.get()));
            return "venta/detalle";
        }
        return "redirect:/venta/listado";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("medicamentos", medicamentoService.getMedicamentos(true));
        model.addAttribute("usuarios", usuarioService.getUsuarios(true));
        return "venta/nueva";
    }

    // Registra la venta con descuento automático de inventario HU-08
    @PostMapping("/guardar")
    public String guardar(@RequestParam Integer idUsuario, @RequestParam Integer idMedicamento,
            @RequestParam Integer cantidad, RedirectAttributes redirectAttributes) {
        var usuario = usuarioService.getUsuario(idUsuario);
        if (usuario.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El usuario seleccionado no existe.");
            return "redirect:/venta/nueva";
        }
        try {
            ventaService.registrar(usuario.get(), idMedicamento, cantidad);
            redirectAttributes.addFlashAttribute("todoOk", "Venta registrada correctamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/venta/nueva";
        }
        return "redirect:/venta/listado";
    }
}
