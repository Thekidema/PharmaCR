package com.pharmacr.service;

import com.pharmacr.domain.EntradaInventario;
import com.pharmacr.repository.EntradaInventarioRepository;
import com.pharmacr.repository.MedicamentoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EntradaInventarioService {

    private final EntradaInventarioRepository entradaInventarioRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final AlertaService alertaService;

    public EntradaInventarioService(EntradaInventarioRepository entradaInventarioRepository,
            MedicamentoRepository medicamentoRepository, AlertaService alertaService) {
        this.entradaInventarioRepository = entradaInventarioRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.alertaService = alertaService;
    }

    @Transactional(readOnly = true)
    public List<EntradaInventario> getEntradas() {
        return entradaInventarioRepository.findAll();
    }

    // Registra la entrada y aumenta el stock automáticamente (HU-11)
    @Transactional
    public void save(EntradaInventario entrada) {
        var medicamento = medicamentoRepository.findById(entrada.getMedicamento().getIdMedicamento());
        if (medicamento.isEmpty()) {
            throw new IllegalArgumentException("El medicamento seleccionado no existe.");
        }
        entradaInventarioRepository.save(entrada);
        var med = medicamento.get();
        med.setStockActual(med.getStockActual() + entrada.getCantidad());
        medicamentoRepository.save(med);
        alertaService.revisar(med);
    }
}
