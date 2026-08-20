package com.pharmacr.controller;

import com.pharmacr.domain.Proveedor;
import com.pharmacr.service.ProveedorService;
import jakarta.validation.Valid;
import java.util.Locale;
import java.util.Optional;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/proveedor")
@PreAuthorize("hasRole('ENCARGADO_INVENTARIO')")
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
        if (!model.containsAttribute("proveedor")) {
            model.addAttribute("proveedor", new Proveedor());
        }
        return "/proveedor/listado";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Proveedor proveedor, BindingResult bindingResult,
            RedirectAttributes redirectAttributes, Locale locale) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    BindingResult.MODEL_KEY_PREFIX + "proveedor", bindingResult);
            redirectAttributes.addFlashAttribute("proveedor", proveedor);
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("proveedor.error04", null, locale));
            if (proveedor.getIdProveedor() == null) {
                redirectAttributes.addFlashAttribute("reabrirModal", "agregarProveedorModal");
                return "redirect:/proveedor/listado";
            }
            return "redirect:/proveedor/modificar/" + proveedor.getIdProveedor();
        }

        String titulo = "todoOk";
        String detalle = "mensaje.actualizado";
        try {
            proveedorService.save(proveedor);
        } catch (DataIntegrityViolationException e) {
                                                  //El nombre comercial es unico: HU-10 pide no permitir proveedores duplicados
            titulo = "error";
            detalle = "proveedor.error05";
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(detalle, null, locale));

        return "redirect:/proveedor/listado";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idProveedor, RedirectAttributes redirectAttributes, Locale locale) {
        String titulo = "todoOk";
        String detalle = "mensaje.eliminado";
        try {
            proveedorService.desactivar(idProveedor);
        } catch (IllegalArgumentException e) {
            titulo = "error";                    // Captura la excepcion de argumento invalido para el mensaje de "no existe"
            detalle = "proveedor.error01";
        } catch (Exception e) {
            titulo = "error";                    // Captura cualquier otra excepcion inesperada
            detalle = "proveedor.error03";
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(detalle, null, locale));
        return "redirect:/proveedor/listado";
    }

    @GetMapping("/modificar/{idProveedor}")
    public String modificar(@PathVariable("idProveedor") Integer idProveedor, Model model, RedirectAttributes redirectAttributes, Locale locale) {
        if (!model.containsAttribute("proveedor")) {
            Optional<Proveedor> proveedorOpt = proveedorService.getProveedor(idProveedor);
            if (proveedorOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("proveedor.error01", null, locale));
                return "redirect:/proveedor/listado";
            }
            model.addAttribute("proveedor", proveedorOpt.get());
        }
        return "/proveedor/modifica";
    }
}
