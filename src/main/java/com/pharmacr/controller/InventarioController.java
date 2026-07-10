package com.pharmacr.controller;

import com.pharmacr.domain.EntradaInventario;
import com.pharmacr.domain.Medicamento;
import com.pharmacr.domain.Proveedor;
import com.pharmacr.domain.SalidaInventario;
import com.pharmacr.domain.Usuario;
import com.pharmacr.service.EntradaInventarioService;
import com.pharmacr.service.MedicamentoService;
import com.pharmacr.service.ProveedorService;
import com.pharmacr.service.SalidaInventarioService;
import com.pharmacr.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/inventario")
public class InventarioController {

    private final EntradaInventarioService entradaInventarioService;
    private final SalidaInventarioService salidaInventarioService;
    private final MedicamentoService medicamentoService;
    private final ProveedorService proveedorService;
    private final UsuarioService usuarioService;

    public InventarioController(EntradaInventarioService entradaInventarioService,
            SalidaInventarioService salidaInventarioService, MedicamentoService medicamentoService,
            ProveedorService proveedorService, UsuarioService usuarioService) {
        this.entradaInventarioService = entradaInventarioService;
        this.salidaInventarioService = salidaInventarioService;
        this.medicamentoService = medicamentoService;
        this.proveedorService = proveedorService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/entradas")
    public String entradas(Model model) {
        var entradas = entradaInventarioService.getEntradas();
        model.addAttribute("entradas", entradas);
        model.addAttribute("totalEntradas", entradas.size());
        return "inventario/entradas";
    }

    // Registro de entrada: aumenta stock automáticamente HU-11
    @GetMapping("/entrada/agregar")
    public String agregarEntrada(Model model) {
        var entrada = new EntradaInventario();
        entrada.setProveedor(new Proveedor());
        entrada.setMedicamento(new Medicamento());
        entrada.setUsuario(new Usuario());
        model.addAttribute("entrada", entrada);
        model.addAttribute("proveedores", proveedorService.getProveedores(true));
        model.addAttribute("medicamentos", medicamentoService.getMedicamentos(true));
        model.addAttribute("usuarios", usuarioService.getUsuarios(true));
        return "inventario/entrada";
    }

    @PostMapping("/entrada/agregar")
    public String agregarEntrada(@ModelAttribute EntradaInventario entrada,
            RedirectAttributes redirectAttributes) {
        try {
            entradaInventarioService.save(entrada);
            redirectAttributes.addFlashAttribute("todoOk", "Entrada registrada y stock actualizado.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventario/entradas";
    }

    @GetMapping("/salidas")
    public String salidas(Model model) {
        var salidas = salidaInventarioService.getSalidas();
        model.addAttribute("salidas", salidas);
        model.addAttribute("totalSalidas", salidas.size());
        return "inventario/salidas";
    }

    // Registro de salida: descuenta stock con validacion HU-12
    @GetMapping("/salida/agregar")
    public String agregarSalida(Model model) {
        var salida = new SalidaInventario();
        salida.setMedicamento(new Medicamento());
        salida.setUsuario(new Usuario());
        model.addAttribute("salida", salida);
        model.addAttribute("medicamentos", medicamentoService.getMedicamentos(true));
        model.addAttribute("usuarios", usuarioService.getUsuarios(true));
        return "inventario/salida";
    }

    @PostMapping("/salida/agregar")
    public String agregarSalida(@ModelAttribute SalidaInventario salida,
            RedirectAttributes redirectAttributes) {
        try {
            salidaInventarioService.save(salida);
            redirectAttributes.addFlashAttribute("todoOk", "Salida registrada y stock actualizado.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventario/salidas";
    }
}
