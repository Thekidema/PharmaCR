package com.pharmacr.service;

import com.pharmacr.domain.SalidaInventario;
import com.pharmacr.repository.MedicamentoRepository;
import com.pharmacr.repository.SalidaInventarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalidaInventarioService {

    private final SalidaInventarioRepository salidaInventarioRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final AlertaService alertaService;

    public SalidaInventarioService(SalidaInventarioRepository salidaInventarioRepository,
            MedicamentoRepository medicamentoRepository, AlertaService alertaService) {
        this.salidaInventarioRepository = salidaInventarioRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.alertaService = alertaService;
    }

    @Transactional(readOnly = true)
    public List<SalidaInventario> getSalidas() {
        return salidaInventarioRepository.findAll();
    }

    // Registra la salida y descuenta el stock; no permite salida mayor al stock (HU-12)
    @Transactional
    public void save(SalidaInventario salida) {
        var medicamento = medicamentoRepository.findById(salida.getMedicamento().getIdMedicamento());
        if (medicamento.isEmpty()) {
            throw new IllegalArgumentException("El medicamento seleccionado no existe.");
        }
        var med = medicamento.get();
        if (salida.getCantidad() > med.getStockActual()) {
            throw new IllegalArgumentException("La cantidad de salida (" + salida.getCantidad()
                    + ") es mayor al stock actual (" + med.getStockActual() + ").");
        }
        salidaInventarioRepository.save(salida);
        med.setStockActual(med.getStockActual() - salida.getCantidad());
        medicamentoRepository.save(med);
        alertaService.revisar(med);
    }
}
