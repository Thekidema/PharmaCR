package com.pharmacr.controller;

import com.pharmacr.domain.Constante;
import com.pharmacr.service.ConstanteService;
import jakarta.validation.Valid;
import java.util.Locale;
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
@RequestMapping("/constante")
@PreAuthorize("hasRole('ADMIN')")
public class ConstanteController {

    private final ConstanteService constanteService;
    private final MessageSource messageSource;

    public ConstanteController(ConstanteService constanteService, MessageSource messageSource) {
        this.constanteService = constanteService;
        this.messageSource = messageSource;
    }

    @GetMapping("/listado")
    public String listado(Model model) {
        var constantes = constanteService.getConstantes();
        model.addAttribute("constantes", constantes);
        model.addAttribute("totalConstantes", constantes.size());
        if (!model.containsAttribute("constante")) {
            model.addAttribute("constante", new Constante());
        }
        return "/constante/listado";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Constante constante, BindingResult bindingResult,
            RedirectAttributes redirectAttributes, Locale locale) {

        if (bindingResult.hasErrors()) {

            redirectAttributes.addFlashAttribute(
                    BindingResult.MODEL_KEY_PREFIX + "constante", bindingResult);
            redirectAttributes.addFlashAttribute("constante", constante);
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("constante.error02", null, locale));
            if (constante.getIdConstante() != null) {
                return "redirect:/constante/modificar/" + constante.getIdConstante();
            }
            return "redirect:/constante/listado";
        }

        String titulo = "todoOk";
        String detalle = "mensaje.actualizado";
        try {
            constanteService.save(constante);
        } catch (DataIntegrityViolationException e) {
  
            titulo = "error";
            detalle = "constante.error03";
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(detalle, null, locale));
        return "redirect:/constante/listado";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idConstante, RedirectAttributes redirectAttributes, Locale locale) {
        String titulo = "todoOk";
        String detalle = "mensaje.eliminado";
        try {
            constanteService.delete(idConstante);
        } catch (IllegalArgumentException e) {
            titulo = "error"; 
            detalle = "constante.error01";
        } catch (Exception e) {
            titulo = "error";  
            detalle = "constante.error04";
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(detalle, null, locale));
        return "redirect:/constante/listado";
    }

    @GetMapping("/modificar/{idConstante}")
    public String modificar(@PathVariable("idConstante") Integer idConstante, Model model,
            RedirectAttributes redirectAttributes, Locale locale) {
        if (!model.containsAttribute("constante")) {
            var constanteOpt = constanteService.getConstante(idConstante);
            if (constanteOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        messageSource.getMessage("constante.error01", null, locale));
                return "redirect:/constante/listado";
            }
            model.addAttribute("constante", constanteOpt.get());
        }
        return "/constante/modifica";
    }
}
