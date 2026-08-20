package com.pharmacr.controller;

import com.pharmacr.service.RecuperacionClaveService;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/recuperar_clave")
public class RecuperacionController {

    private final RecuperacionClaveService recuperacionClaveService;
    private final MessageSource messageSource;

    public RecuperacionController(RecuperacionClaveService recuperacionClaveService, MessageSource messageSource) {
        this.recuperacionClaveService = recuperacionClaveService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String solicitar() {
        return "/recuperar_clave/solicitar";
    }

    @PostMapping("/solicitar")
    public String solicitar(@RequestParam String correo, Locale locale, RedirectAttributes redirectAttributes) {
        recuperacionClaveService.solicitarCodigo(correo);
                                                             //Mensaje identico exista o no la cuenta: evita enumeracion de usuarios
        redirectAttributes.addFlashAttribute("todoOk",
                messageSource.getMessage("recuperar.mensaje.enviado", null, locale));
        redirectAttributes.addAttribute("correo", correo);
        return "redirect:/recuperar_clave/confirmar";
    }

    @GetMapping("/confirmar")
    public String confirmar(@RequestParam(required = false) String correo, Model model) {
        model.addAttribute("correo", correo);
        return "/recuperar_clave/confirmar";
    }

    @PostMapping("/confirmar")
    public String confirmar(@RequestParam String correo, @RequestParam String codigo,
            @RequestParam String nuevaClave, @RequestParam String confirmarClave,
            Locale locale, RedirectAttributes redirectAttributes) {

        if (!nuevaClave.equals(confirmarClave)) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("recuperar.error02", null, locale));
            redirectAttributes.addAttribute("correo", correo);
            return "redirect:/recuperar_clave/confirmar";
        }
        try {
            recuperacionClaveService.confirmarReseteo(correo, codigo, nuevaClave);
            redirectAttributes.addFlashAttribute("todoOk",
                    messageSource.getMessage("recuperar.mensaje.exito", null, locale));
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("recuperar.error01", null, locale));
            redirectAttributes.addAttribute("correo", correo);
            return "redirect:/recuperar_clave/confirmar";
        }
    }
}
