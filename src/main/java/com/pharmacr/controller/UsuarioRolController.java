package com.pharmacr.controller;

import com.pharmacr.service.UsuarioService;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


 // HU-03: asignar y modificar roles de usuario.

@Controller
@RequestMapping("/usuario_rol")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioRolController {

    private final UsuarioService usuarioService;
    private final MessageSource messageSource;

    public UsuarioRolController(UsuarioService usuarioService, MessageSource messageSource) {
        this.usuarioService = usuarioService;
        this.messageSource = messageSource;
    }

    @GetMapping("/mantenimiento")
    public String mantenimiento(Model model) {
        model.addAttribute("usuarios", usuarioService.getUsuarios(true));
        return "/usuario_rol/mantenimiento";
    }

    @GetMapping("/buscar")
    public String buscar(@RequestParam String username, Model model,
            RedirectAttributes redirectAttributes, Locale locale) {

        var usuarioOpt = usuarioService.getUsuario(username);
        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("role.error01", null, locale));
            return "redirect:/usuario_rol/mantenimiento";
        }

        model.addAttribute("usuarios", usuarioService.getUsuarios(true));
        model.addAttribute("usuario", usuarioOpt.get());
        model.addAttribute("rolesAsignados", usuarioService.getRolesAsignados(username));
        model.addAttribute("rolesDisponibles", usuarioService.getRolesDisponibles(username));
        return "/usuario_rol/mantenimiento";
    }

    @PostMapping("/agregarRol")
    public String agregarRol(@RequestParam String username, @RequestParam Integer idRol,
            RedirectAttributes redirectAttributes, Locale locale) {

        String titulo = "todoOk";
        String detalle = "role.asignado";
        try {
            usuarioService.asignarRol(username, idRol);
        } catch (IllegalArgumentException e) {
            titulo = "error";
            detalle = "role.error01";
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(detalle, null, locale));
        return "redirect:/usuario_rol/buscar?username=" + username;
    }

    @PostMapping("/eliminarRol")
    public String eliminarRol(@RequestParam String username, @RequestParam Integer idRol,
            RedirectAttributes redirectAttributes, Locale locale) {

        String titulo = "todoOk";
        String detalle = "role.eliminado";
        try {
            usuarioService.eliminarRol(username, idRol);
        } catch (IllegalArgumentException e) {
            titulo = "error";
            detalle = "role.error01";
        } catch (IllegalStateException e) {
            //Se intento dejar al usuario sin ningun rol
            titulo = "error";
            detalle = "role.error02";
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(detalle, null, locale));
        return "redirect:/usuario_rol/buscar?username=" + username;
    }
}
