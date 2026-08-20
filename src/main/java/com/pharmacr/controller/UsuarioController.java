package com.pharmacr.controller;

import com.pharmacr.domain.Usuario;
import com.pharmacr.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuario")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final MessageSource messageSource;

    public UsuarioController(UsuarioService usuarioService, MessageSource messageSource) {
        this.usuarioService = usuarioService;
        this.messageSource = messageSource;
    }

    @GetMapping("/listado")
    public String listado(Model model) {
        var usuarios = usuarioService.getUsuarios(false);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("totalUsuarios", usuarios.size());
        //Si guardar() flasheo un usuario con errores de validacion, se respeta
        //ese objeto en vez de pisarlo con uno vacio
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
        model.addAttribute("roles", usuarioService.getRoles());
        return "/usuario/listado";
    }

      //Asignacion de roles HU-02 / HU-03
    @PostMapping("/guardar")
    public String guardar(@Valid Usuario usuario, BindingResult bindingResult,
            @RequestParam(required = false) List<Integer> idsRoles,
            @RequestParam(name = "imagenFile", required = false) MultipartFile imagenFile,
            RedirectAttributes redirectAttributes, Locale locale) {

        if (bindingResult.hasErrors()) {
            //Se conservan el objeto y los errores a traves del redirect (patron
            redirectAttributes.addFlashAttribute(
                    BindingResult.MODEL_KEY_PREFIX + "usuario", bindingResult);
            redirectAttributes.addFlashAttribute("usuario", usuario);
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("usuario.error04", null, locale));
            if (usuario.getIdUsuario() == null) {
                redirectAttributes.addFlashAttribute("reabrirModal", "agregarUsuarioModal");
                return "redirect:/usuario/listado";
            }
            return "redirect:/usuario/modificar/" + usuario.getIdUsuario();
        }

        String titulo = "todoOk";
        String detalle = "mensaje.actualizado";
        try {
            usuario.setRoles(new HashSet<>(usuarioService.getRoles(idsRoles)));
            usuarioService.save(usuario, imagenFile, true);
        } catch (DataIntegrityViolationException e) {
            //Se violo el unique de username o de correo
            titulo = "error";
            detalle = "usuario.error05";
        } catch (IllegalStateException e) {
            //Fallo la subida de la foto a Firebase
            titulo = "error";
            detalle = "usuario.error06";
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(detalle, null, locale));

        return "redirect:/usuario/listado";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idUsuario, RedirectAttributes redirectAttributes, Locale locale) {
        String titulo = "todoOk";
        String detalle = "mensaje.eliminado";
        try {
            usuarioService.desactivar(idUsuario);
        } catch (IllegalArgumentException e) {
            titulo = "error"; // Este hace excepcion de argumento invalido para el mensaje de "no existe"
            detalle = "usuario.error01";
        } catch (Exception e) {
            titulo = "error";  // Captura cualquier otra excepcion inesperada
            detalle = "usuario.error03";
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(detalle, null, locale));
        return "redirect:/usuario/listado";
    }

    @GetMapping("/modificar/{idUsuario}")
    public String modificar(@PathVariable("idUsuario") Integer idUsuario, Model model,
            RedirectAttributes redirectAttributes, Locale locale) {
        //Si guardar() ya flasheo el usuario con errores de validacion, se respeta
        //ese objeto en vez de sobreescribirlo con una relectura limpia de BD
        if (!model.containsAttribute("usuario")) {
            Optional<Usuario> usuarioOpt = usuarioService.getUsuario(idUsuario);
            if (usuarioOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("usuario.error01", null, locale));
                return "redirect:/usuario/listado";
            }
            var usuario = usuarioOpt.get();
            usuario.setPassword(""); //Nunca se manda la clave real al formulario
            model.addAttribute("usuario", usuario);
        }
        model.addAttribute("roles", usuarioService.getRoles());
        return "/usuario/modifica";
    }
}
