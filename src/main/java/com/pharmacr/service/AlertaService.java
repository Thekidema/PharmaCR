package com.pharmacr.service;

import com.pharmacr.domain.Alerta;
import com.pharmacr.domain.Medicamento;
import com.pharmacr.repository.AlertaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertaService {

    private final AlertaRepository alertaRepository;

    public AlertaService(AlertaRepository alertaRepository) {
        this.alertaRepository = alertaRepository;
    }

    @Transactional(readOnly = true)
    public List<Alerta> getAlertas(boolean activo) {
        if (activo) { //Solo quiero las alertas activas
            return alertaRepository.findByActivoTrue();
        }
        return alertaRepository.findAll();
    }

    // Revisa lo que el stock del medicamento crea la alerta si está bajo el mínimo
    // y la desactiva cuando el stock se repone HU-16
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
            }
        } else {
            for (Alerta alerta : alertasActivas) {
                alerta.setActivo(false);
                alertaRepository.save(alerta);
            }
        }
    }
}
