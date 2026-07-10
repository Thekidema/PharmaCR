package com.pharmacr.controller;

import com.pharmacr.domain.Proveedor;
import com.pharmacr.service.ProveedorService;
import jakarta.validation.Valid;
import java.util.Locale;
import java.util.Optional;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/proveedor")
public class ProveedorController {

    private final ProveedorService proveedorService;
    private final MessageSource messageSource;

    public ProveedorController(ProveedorService proveedorService, MessageSource messageSource) {
        this.proveedorService = proveedorService;
        this.messageSource = messageSource;
    }

    @GetMapping("/listado")
    public String listado(Model model) {
        var proveedores = proveedorService.getProveedores(false);
        model.addAttribute("proveedores", proveedores);
        model.addAttribute("totalProveedores", proveedores.size());
        model.addAttribute("proveedor", new Proveedor());
        return "/proveedor/listado";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Proveedor proveedor, RedirectAttributes redirectAttributes) {

        proveedorService.save(proveedor);
        redirectAttributes.addFlashAttribute("todoOk", messageSource.getMessage("mensaje.actualizado", null, Locale.getDefault()));

        return "redirect:/proveedor/listado";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idProveedor, RedirectAttributes redirectAttributes) {
        String titulo = "todoOk";
        String detalle = "mensaje.eliminado";
        try {
            proveedorService.delete(idProveedor);
        } catch (IllegalArgumentException e) {
            titulo = "error"; // Captura la excepción de argumento inválido para el mensaje de "no existe"
            detalle = "proveedor.error01";
        } catch (IllegalStateException e) {
            titulo = "error"; // Captura la excepción de estado ilegal para el mensaje de "datos asociados"
            detalle = "proveedor.error02";
        } catch (Exception e) {
            titulo = "error";  // Captura cualquier otra excepción inesperada
            detalle = "proveedor.error03";
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(detalle, null, Locale.getDefault()));
        return "redirect:/proveedor/listado";
    }

    @GetMapping("/modificar/{idProveedor}")
    public String modificar(@PathVariable("idProveedor") Integer idProveedor, Model model, RedirectAttributes redirectAttributes) {
        Optional<Proveedor> proveedorOpt = proveedorService.getProveedor(idProveedor);
        if (proveedorOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("proveedor.error01", null, Locale.getDefault()));
            return "redirect:/proveedor/listado";
        }
        model.addAttribute("proveedor", proveedorOpt.get());
        return "/proveedor/modifica";
    }
}
