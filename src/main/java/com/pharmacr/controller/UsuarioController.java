package com.pharmacr.controller;

import com.pharmacr.domain.Usuario;
import com.pharmacr.service.RolService;
import com.pharmacr.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final MessageSource messageSource;
    private final RolService rolService;

    public UsuarioController(UsuarioService usuarioService, MessageSource messageSource,
            RolService rolService) {
        this.usuarioService = usuarioService;
        this.messageSource = messageSource;
        this.rolService = rolService;
    }

    @GetMapping("/listado")
    public String listado(Model model) {
        var usuarios = usuarioService.getUsuarios(false);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("totalUsuarios", usuarios.size());
        model.addAttribute("usuario", new Usuario());
        var roles = rolService.getRoles();
        model.addAttribute("roles", roles);
        return "/usuario/listado";
    }

    //Asignacion de roles via checkboxes del formulario HU-02 / HU-03
    @PostMapping("/guardar")
    public String guardar(@Valid Usuario usuario,
            @RequestParam(required = false) List<Integer> idsRoles,
            RedirectAttributes redirectAttributes) {

        usuario.setRoles(rolService.getRoles(idsRoles));
        usuarioService.save(usuario);
        redirectAttributes.addFlashAttribute("todoOk", messageSource.getMessage("mensaje.actualizado", null, Locale.getDefault()));

        return "redirect:/usuario/listado";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idUsuario, RedirectAttributes redirectAttributes) {
        String titulo = "todoOk";
        String detalle = "mensaje.eliminado";
        try {
            usuarioService.delete(idUsuario);
        } catch (IllegalArgumentException e) {
            titulo = "error"; // Este hace excepcion de argumento invalido para el mensaje de "no existe"
            detalle = "usuario.error01";
        } catch (IllegalStateException e) {
            titulo = "error"; // Este hace excepción de estado ilegal para el mensaje de "datos asociados"
            detalle = "usuario.error02";
        } catch (Exception e) {
            titulo = "error";  // Captura cualquier otra excepción inesperada
            detalle = "usuario.error03";
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(detalle, null, Locale.getDefault()));
        return "redirect:/usuario/listado";
    }

    @GetMapping("/modificar/{idUsuario}")
    public String modificar(@PathVariable("idUsuario") Integer idUsuario, Model model, RedirectAttributes redirectAttributes) {
        Optional<Usuario> usuarioOpt = usuarioService.getUsuario(idUsuario);
        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("usuario.error01", null, Locale.getDefault()));
            return "redirect:/usuario/listado";
        }
        model.addAttribute("usuario", usuarioOpt.get());
        var roles = rolService.getRoles();
        model.addAttribute("roles", roles);
        return "/usuario/modifica";
    }
}
