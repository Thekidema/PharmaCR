package com.pharmacr.controller;

import com.pharmacr.domain.Medicamento;
import com.pharmacr.service.CategoriaMedicamentoService;
import com.pharmacr.service.EntradaInventarioService;
import com.pharmacr.service.MedicamentoService;
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
@RequestMapping("/medicamento")
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
        model.addAttribute("medicamento", new Medicamento());
        return "/medicamento/listado";
    }

    //Búsqueda por nombre o código, solo activos (HU-09)
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
        model.addAttribute("entradas", entradaInventarioService.getEntradas());
        return "/medicamento/disponibilidad";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Medicamento medicamento, RedirectAttributes redirectAttributes) {

        medicamentoService.save(medicamento);
        redirectAttributes.addFlashAttribute("todoOk", messageSource.getMessage("mensaje.actualizado", null, Locale.getDefault()));

        return "redirect:/medicamento/listado";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idMedicamento, RedirectAttributes redirectAttributes) {
        String titulo = "todoOk";
        String detalle = "mensaje.eliminado";
        try {
            medicamentoService.delete(idMedicamento);
        } catch (IllegalArgumentException e) {
            titulo = "error"; // Captura la excepción de argumento inválido para el mensaje de "no existe"
            detalle = "medicamento.error01";
        } catch (IllegalStateException e) {
            titulo = "error"; // Captura la excepción de estado ilegal para el mensaje de "datos asociados"
            detalle = "medicamento.error02";
        } catch (Exception e) {
            titulo = "error";  // Captura cualquier otra excepción inesperada
            detalle = "medicamento.error03";
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(detalle, null, Locale.getDefault()));
        return "redirect:/medicamento/listado";
    }

    @GetMapping("/modificar/{idMedicamento}")
    public String modificar(@PathVariable("idMedicamento") Integer idMedicamento, Model model, RedirectAttributes redirectAttributes) {
        Optional<Medicamento> medicamentoOpt = medicamentoService.getMedicamento(idMedicamento);
        if (medicamentoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("medicamento.error01", null, Locale.getDefault()));
            return "redirect:/medicamento/listado";
        }
        model.addAttribute("medicamento", medicamentoOpt.get());
        var categorias = categoriaMedicamentoService.getCategorias(true);
        model.addAttribute("categorias", categorias);
        return "/medicamento/modifica";
    }

    //Desactivación (soft-delete) de medicamentos descontinuados (HU-06)
    @GetMapping("/desactivar/{idMedicamento}")
    public String desactivar(@PathVariable("idMedicamento") Integer idMedicamento, RedirectAttributes redirectAttributes) {
        try {
            medicamentoService.desactivar(idMedicamento);
            redirectAttributes.addFlashAttribute("todoOk", messageSource.getMessage("medicamento.desactivado", null, Locale.getDefault()));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("medicamento.error01", null, Locale.getDefault()));
        }
        return "redirect:/medicamento/listado";
    }
}
