package com.pharmacr.controller;

import com.pharmacr.domain.Medicamento;
import com.pharmacr.service.CategoriaMedicamentoService;
import com.pharmacr.service.EntradaInventarioService;
import com.pharmacr.service.MedicamentoService;
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
@RequestMapping("/medicamento")
@PreAuthorize("hasAnyRole('ENCARGADO_INVENTARIO','FARMACEUTICO')")
public class MedicamentoController {

    private final MedicamentoService medicamentoService;
    private final MessageSource messageSource;
    private final CategoriaMedicamentoService categoriaMedicamentoService;
    private final EntradaInventarioService entradaInventarioService;

    public MedicamentoController(MedicamentoService medicamentoService, MessageSource messageSource,
            CategoriaMedicamentoService categoriaMedicamentoService,
            EntradaInventarioService entradaInventarioService) {
        this.medicamentoService = medicamentoService;
        this.messageSource = messageSource;
        this.categoriaMedicamentoService = categoriaMedicamentoService;
        this.entradaInventarioService = entradaInventarioService;
    }

    @GetMapping("/listado")
    public String listado(Model model) {
        var medicamentos = medicamentoService.getMedicamentos(false);
        model.addAttribute("medicamentos", medicamentos);
        model.addAttribute("totalMedicamentos", medicamentos.size());
        var categorias = categoriaMedicamentoService.getCategorias(true);
        model.addAttribute("categorias", categorias);
        if (!model.containsAttribute("medicamento")) {
            model.addAttribute("medicamento", new Medicamento());
        }
        return "/medicamento/listado";
    }

    //Busqueda por nombre o codigo, solo activos (HU-09)
    @GetMapping("/buscar")
    public String buscar(@RequestParam String termino, Model model) {
        var medicamentos = medicamentoService.buscar(termino);
        model.addAttribute("medicamentos", medicamentos);
        model.addAttribute("totalMedicamentos", medicamentos.size());
        model.addAttribute("termino", termino);
        var categorias = categoriaMedicamentoService.getCategorias(true);
        model.addAttribute("categorias", categorias);
        model.addAttribute("medicamento", new Medicamento());
        return "/medicamento/listado";
    }

    //Consulta de disponibilidad con lote y vencimiento (HU-07)
    @GetMapping("/disponibilidad")
    public String disponibilidad(Model model) {
        model.addAttribute("medicamentos", medicamentoService.getMedicamentos(true));
        //Los lotes vienen agrupados por medicamento y ordenados por vencimiento:
        //el primero de cada lista es el que vence mas pronto
        model.addAttribute("lotes", medicamentoService.getLotesAgrupados());
        return "/medicamento/disponibilidad";
    }

    @PreAuthorize("hasRole('ENCARGADO_INVENTARIO')")
    @PostMapping("/guardar")
    public String guardar(@Valid Medicamento medicamento, BindingResult bindingResult,
            RedirectAttributes redirectAttributes, Locale locale) {

        if (bindingResult.hasErrors()) {
            //Se conservan el objeto y los errores a traves del redirect (patron
            //POST-Redirect-GET) para que el usuario no pierda lo que ya tecleo
            redirectAttributes.addFlashAttribute(
                    BindingResult.MODEL_KEY_PREFIX + "medicamento", bindingResult);
            redirectAttributes.addFlashAttribute("medicamento", medicamento);
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("medicamento.error04", null, locale));
            if (medicamento.getIdMedicamento() == null) {
                redirectAttributes.addFlashAttribute("reabrirModal", "agregarMedicamentoModal");
                return "redirect:/medicamento/listado";
            }
            return "redirect:/medicamento/modificar/" + medicamento.getIdMedicamento();
        }

        String titulo = "todoOk";
        String detalle = "mensaje.actualizado";
        try {
            medicamentoService.save(medicamento);
        } catch (DataIntegrityViolationException e) {
            titulo = "error";
            detalle = "medicamento.error05";
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(detalle, null, locale));

        return "redirect:/medicamento/listado";
    }

    @PreAuthorize("hasRole('ENCARGADO_INVENTARIO')")
    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idMedicamento, RedirectAttributes redirectAttributes, Locale locale) {
        String titulo = "todoOk";
        String detalle = "mensaje.eliminado";
        try {
            medicamentoService.desactivar(idMedicamento);
        } catch (IllegalArgumentException e) {
            titulo = "error";                   // Captura la excepcion de argumento invalido para el mensaje de "no existe"
            detalle = "medicamento.error01";
        } catch (Exception e) {
            titulo = "error";                   // Captura cualquier otra excepcion inesperada
            detalle = "medicamento.error03";
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(detalle, null, locale));
        return "redirect:/medicamento/listado";
    }

    @PreAuthorize("hasRole('ENCARGADO_INVENTARIO')")
    @GetMapping("/modificar/{idMedicamento}")
    public String modificar(@PathVariable("idMedicamento") Integer idMedicamento, Model model, RedirectAttributes redirectAttributes, Locale locale) {
                                                  //Si guardar() ya flasheo el medicamento con errores de validacion se
        if (!model.containsAttribute("medicamento")) {
            Optional<Medicamento> medicamentoOpt = medicamentoService.getMedicamento(idMedicamento);
            if (medicamentoOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("medicamento.error01", null, locale));
                return "redirect:/medicamento/listado";
            }
            model.addAttribute("medicamento", medicamentoOpt.get());
        }
        var categorias = categoriaMedicamentoService.getCategorias(true);
        model.addAttribute("categorias", categorias);
        return "/medicamento/modifica";
    }
}
