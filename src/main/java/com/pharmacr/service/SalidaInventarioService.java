package com.pharmacr.service;

import com.pharmacr.domain.SalidaInventario;
import com.pharmacr.domain.TipoMovimiento;
import com.pharmacr.domain.Usuario;
import com.pharmacr.repository.SalidaInventarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalidaInventarioService {

    private final SalidaInventarioRepository salidaInventarioRepository;
    private final MedicamentoService medicamentoService;

    public SalidaInventarioService(SalidaInventarioRepository salidaInventarioRepository,
            MedicamentoService medicamentoService) {
        this.salidaInventarioRepository = salidaInventarioRepository;
        this.medicamentoService = medicamentoService;
    }

    @Transactional(readOnly = true)
    public List<SalidaInventario> getSalidas() {
        return salidaInventarioRepository.findAllConRelaciones();
    }

    // Registra la salida y descuenta el stock; no permite salida mayor al stock (HU-12)
    @Transactional
    public void save(SalidaInventario salida, Usuario usuario) {
        if (salida.getMedicamento() == null || salida.getMedicamento().getIdMedicamento() == null) {
            throw new IllegalArgumentException("Debe seleccionar un medicamento.");
        }
        var medicamento = medicamentoService.getMedicamento(salida.getMedicamento().getIdMedicamento());
        if (medicamento.isEmpty()) {
            throw new IllegalArgumentException("El medicamento seleccionado no existe.");
        }
        var med = medicamento.get();
        if (med.getStockActual() == null || salida.getCantidad() > med.getStockActual()) {
            throw new IllegalArgumentException("La cantidad de salida (" + salida.getCantidad()
                    + ") es mayor al stock actual (" + med.getStockActual() + ").");
        }
        //El responsable es el usuario autenticado, no uno escogido en el formulario
        salida.setUsuario(usuario);
        salidaInventarioRepository.save(salida);
        medicamentoService.ajustarStockYRegistrar(med.getIdMedicamento(), salida.getCantidad(), false,
                "La cantidad de salida (" + salida.getCantidad()
                        + ") es mayor al stock actual: el inventario cambió mientras se procesaba la salida.",
                usuario, TipoMovimiento.Salida, salida.getTipo() + ": " + salida.getMotivo(), salida.getIdSalida());
    }
}
