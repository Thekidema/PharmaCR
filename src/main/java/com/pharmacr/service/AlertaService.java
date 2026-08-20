package com.pharmacr.service;

import com.pharmacr.domain.Alerta;
import com.pharmacr.domain.Medicamento;
import com.pharmacr.repository.AlertaRepository;
import com.pharmacr.repository.UsuarioRepository;
import jakarta.mail.MessagingException;
import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CorreoService correoService;
    private final MessageSource messageSource;

    public AlertaService(AlertaRepository alertaRepository, UsuarioRepository usuarioRepository,
            CorreoService correoService, MessageSource messageSource) {
        this.alertaRepository = alertaRepository;
        this.usuarioRepository = usuarioRepository;
        this.correoService = correoService;
        this.messageSource = messageSource;
    }

    @Transactional(readOnly = true)
    public List<Alerta> getAlertas(boolean activo) {
        if (activo) { //Solo quiero las alertas activas
            return alertaRepository.findByActivoTrue();
        }
        return alertaRepository.findAll();
    }

    // Revisa lo que el stock del medicamento crea la alerta si esta bajo el minimo y la desactiva cuando el stock se repone HU-16
    @Transactional
    public void revisar(Medicamento medicamento) {
        var alertasActivas = alertaRepository.findByMedicamentoAndActivoTrue(medicamento);
        if (medicamento.getStockActual() < medicamento.getStockMinimo()) {
            if (alertasActivas.isEmpty()) {
                var alerta = new Alerta();
                alerta.setMedicamento(medicamento);
                alerta.setMensaje(medicamento.getNombre() + " está por debajo del stock mínimo (stock: "
                        + medicamento.getStockActual() + ", mínimo: " + medicamento.getStockMinimo() + ")");
                alerta.setActivo(true);
                alertaRepository.save(alerta);
                notificarPorCorreo(medicamento);
            }
        } else {
            for (Alerta alerta : alertasActivas) {
                alerta.setActivo(false);
                alertaRepository.save(alerta);
            }
        }
    }

    // Avisa por correo al encargado de inventario cuando nace una alerta nueva (HU-16).
   
    private void notificarPorCorreo(Medicamento medicamento) {
        var encargados = usuarioRepository.findActivosPorRol("ENCARGADO_INVENTARIO");
        var locale = Locale.getDefault();
        String asunto = String.format(
                messageSource.getMessage("alerta.correo.asunto", null, locale), medicamento.getNombre());
        for (var encargado : encargados) {
            try {
                String cuerpo = String.format(messageSource.getMessage("alerta.correo.cuerpo", null, locale),
                        encargado.getNombre(), medicamento.getNombre(),
                        medicamento.getStockActual(), medicamento.getStockMinimo());
                correoService.enviarCorreoHtml(encargado.getCorreo(), asunto, cuerpo);
            } catch (MessagingException | NoSuchMessageException e) {
            }
        }
    }
}
