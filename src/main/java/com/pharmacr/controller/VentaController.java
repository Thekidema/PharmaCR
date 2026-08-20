package com.pharmacr.controller;

import com.pharmacr.domain.Usuario;
import com.pharmacr.service.CarritoService;
import com.pharmacr.service.FacturaService;
import com.pharmacr.service.MedicamentoService;
import com.pharmacr.service.UsuarioService;
import com.pharmacr.service.VentaService;
import java.security.Principal;
import java.util.Locale;
import java.util.Optional;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
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
@PreAuthorize("hasRole('FARMACEUTICO')")
public class VentaController {

    private final VentaService ventaService;
    private final MedicamentoService medicamentoService;
    private final UsuarioService usuarioService;
    private final CarritoService carritoService;
    private final FacturaService facturaService;
    private final MessageSource messageSource;

    public VentaController(VentaService ventaService, MedicamentoService medicamentoService,
            UsuarioService usuarioService, CarritoService carritoService, FacturaService facturaService,
            MessageSource messageSource) {
        this.ventaService = ventaService;
        this.medicamentoService = medicamentoService;
        this.usuarioService = usuarioService;
        this.carritoService = carritoService;
        this.facturaService = facturaService;
        this.messageSource = messageSource;
    }

    @GetMapping("/listado")
    public String listado(Model model) {
        var ventas = ventaService.getVentas();
        model.addAttribute("ventas", ventas);
        model.addAttribute("totalVentas", ventas.size());
        return "/venta/listado";
    }

    @GetMapping("/detalle/{idVenta}")
    public String detalle(@PathVariable Integer idVenta, Model model) {
        var venta = ventaService.getVenta(idVenta);
        if (venta.isPresent()) {
            model.addAttribute("venta", venta.get());
            model.addAttribute("detalles", ventaService.getDetalles(venta.get()));
            return "/venta/detalle";
        }
        return "redirect:/venta/listado";
    }

    //HU-18: comprobante imprimible, sin menu ni botones al enviarlo a la impresora
    @GetMapping("/comprobante/{idVenta}")
    public String comprobante(@PathVariable Integer idVenta, Model model) {
        var venta = ventaService.getVenta(idVenta);
        if (venta.isEmpty()) {
            return "redirect:/venta/listado";
        }
        model.addAttribute("venta", venta.get());
        model.addAttribute("detalles", ventaService.getDetalles(venta.get()));
        return "/venta/comprobante";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("medicamentos", medicamentoService.getMedicamentos(true));
        model.addAttribute("carrito", carritoService.obtenerCarrito());
        model.addAttribute("totalCarrito", carritoService.calcularTotal());
        model.addAttribute("unidades", carritoService.contarUnidades());
        return "/venta/nueva";
    }

    //HU-08: se van agregando medicamentos al carrito antes de registrar la venta
    @PostMapping("/agregar")
    public String agregar(@RequestParam Integer idMedicamento, @RequestParam Integer cantidad,
            RedirectAttributes redirectAttributes) {
        try {
            carritoService.agregarMedicamento(idMedicamento, cantidad);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/venta/nueva";
    }

    @PostMapping("/eliminarItem")
    public String eliminarItem(@RequestParam Integer idMedicamento) {
        carritoService.eliminarItem(idMedicamento);
        return "redirect:/venta/nueva";
    }

    @PostMapping("/vaciar")
    public String vaciar() {
        carritoService.vaciarCarrito();
        return "redirect:/venta/nueva";
    }

    
     //Registra la venta con descuento automatico de inventario HU-08

    @PostMapping("/guardar")
    public String guardar(@RequestParam(required = false) String correoCliente,
            Principal principal, Locale locale, RedirectAttributes redirectAttributes) {
        var usuario = usuarioActual(principal, locale, redirectAttributes);
        if (usuario.isEmpty()) {
            return "redirect:/venta/nueva";
        }
        var itemsDelCarrito = carritoService.obtenerCarrito();
        carritoService.vaciarCarrito();
        try {
            var venta = ventaService.registrar(usuario.get(), itemsDelCarrito, correoCliente);
            boolean correoOk = facturaService.enviarFactura(venta, ventaService.getDetalles(venta), correoCliente);
            if (correoCliente != null && !correoCliente.isBlank() && !correoOk) {
                redirectAttributes.addFlashAttribute("todoOk",
                        messageSource.getMessage("venta.registrada.correoFallido", null, locale));
            } else {
                redirectAttributes.addFlashAttribute("todoOk",
                        messageSource.getMessage("venta.registrada", null, locale));
            }
            return "redirect:/venta/comprobante/" + venta.getIdVenta();
        } catch (IllegalArgumentException e) {
            carritoService.restaurarCarrito(itemsDelCarrito);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/venta/nueva";
        }
    }

    //Anula una venta ya registrada y repone el stock contraparte de HU-08
    @PostMapping("/anular")
    public String anular(@RequestParam Integer idVenta, Principal principal,
            RedirectAttributes redirectAttributes, Locale locale) {
        var usuario = usuarioActual(principal, locale, redirectAttributes);
        if (usuario.isEmpty()) {
            return "redirect:/venta/listado";
        }
        try {
            ventaService.anular(idVenta, usuario.get());
            redirectAttributes.addFlashAttribute("todoOk", messageSource.getMessage("venta.anulada", null, locale));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("venta.error01", null, locale));
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("venta.error02", null, locale));
        }
        return "redirect:/venta/listado";
    }
    private Optional<Usuario> usuarioActual(Principal principal, Locale locale, RedirectAttributes redirectAttributes) {
        var usuario = usuarioService.getUsuario(principal.getName());
        if (usuario.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("sesion.error01", null, locale));
        }
        return usuario;
    }
}
