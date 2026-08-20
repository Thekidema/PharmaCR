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
import java.security.Principal;
import java.util.Locale;
import java.util.Optional;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/inventario")
@PreAuthorize("hasRole('ENCARGADO_INVENTARIO')")
public class InventarioController {

    private final EntradaInventarioService entradaInventarioService;
    private final SalidaInventarioService salidaInventarioService;
    private final MedicamentoService medicamentoService;
    private final ProveedorService proveedorService;
    private final UsuarioService usuarioService;
    private final MessageSource messageSource;

    public InventarioController(EntradaInventarioService entradaInventarioService,
            SalidaInventarioService salidaInventarioService, MedicamentoService medicamentoService,
            ProveedorService proveedorService, UsuarioService usuarioService, MessageSource messageSource) {
        this.entradaInventarioService = entradaInventarioService;
        this.salidaInventarioService = salidaInventarioService;
        this.medicamentoService = medicamentoService;
        this.proveedorService = proveedorService;
        this.usuarioService = usuarioService;
        this.messageSource = messageSource;
    }

    @GetMapping("/entrada/listado")
    public String entradaListado(Model model) {
        var entradas = entradaInventarioService.getEntradas();
        model.addAttribute("entradas", entradas);
        model.addAttribute("totalEntradas", entradas.size());
        return "/inventario/entrada/listado";
    }

    // Registro de entrada: aumenta stock automaticamente HU-11
    @GetMapping("/entrada/nueva")
    public String entradaNueva(Model model) {
        var entrada = new EntradaInventario();
        entrada.setProveedor(new Proveedor());
        entrada.setMedicamento(new Medicamento());
        entrada.setUsuario(new Usuario());
        model.addAttribute("entrada", entrada);
        model.addAttribute("proveedores", proveedorService.getProveedores(true));
        model.addAttribute("medicamentos", medicamentoService.getMedicamentos(true));
        return "/inventario/entrada/nueva";
    }

    @PostMapping("/entrada/guardar")
    public String entradaGuardar(@ModelAttribute EntradaInventario entrada,
            Principal principal, Locale locale, RedirectAttributes redirectAttributes) {
        var usuario = usuarioActual(principal, locale, redirectAttributes);
        if (usuario.isPresent()) {
            try {
                entradaInventarioService.save(entrada, usuario.get());
                redirectAttributes.addFlashAttribute("todoOk",
                        messageSource.getMessage("inventario.entrada.registrada", null, locale));
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
            }
        }
        return "redirect:/inventario/entrada/listado";
    }

    @GetMapping("/salida/listado")
    public String salidaListado(Model model) {
        var salidas = salidaInventarioService.getSalidas();
        model.addAttribute("salidas", salidas);
        model.addAttribute("totalSalidas", salidas.size());
        return "/inventario/salida/listado";
    }

    // Registro de salida: descuenta stock con validacion HU-12
    @GetMapping("/salida/nueva")
    public String salidaNueva(Model model) {
        var salida = new SalidaInventario();
        salida.setMedicamento(new Medicamento());
        salida.setUsuario(new Usuario());
        model.addAttribute("salida", salida);
        model.addAttribute("medicamentos", medicamentoService.getMedicamentos(true));
        return "/inventario/salida/nueva";
    }

    @PostMapping("/salida/guardar")
    public String salidaGuardar(@ModelAttribute SalidaInventario salida,
            Principal principal, Locale locale, RedirectAttributes redirectAttributes) {
        var usuario = usuarioActual(principal, locale, redirectAttributes);
        if (usuario.isPresent()) {
            try {
                salidaInventarioService.save(salida, usuario.get());
                redirectAttributes.addFlashAttribute("todoOk",
                        messageSource.getMessage("inventario.salida.registrada", null, locale));
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
            }
        }
        return "redirect:/inventario/salida/listado";
    }

    //Resuelve el usuario autenticado; si no se puede identificar, flashea el
    private Optional<Usuario> usuarioActual(Principal principal, Locale locale, RedirectAttributes redirectAttributes) {
        var usuario = usuarioService.getUsuario(principal.getName());
        if (usuario.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("sesion.error01", null, locale));
        }
        return usuario;
    }
}
