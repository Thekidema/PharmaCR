package com.pharmacr.service;

import com.pharmacr.domain.EntradaInventario;
import com.pharmacr.domain.TipoMovimiento;
import com.pharmacr.domain.Usuario;
import com.pharmacr.repository.EntradaInventarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EntradaInventarioService {

    private final EntradaInventarioRepository entradaInventarioRepository;
    private final MedicamentoService medicamentoService;

    public EntradaInventarioService(EntradaInventarioRepository entradaInventarioRepository,
            MedicamentoService medicamentoService) {
        this.entradaInventarioRepository = entradaInventarioRepository;
        this.medicamentoService = medicamentoService;
    }

    @Transactional(readOnly = true)
    public List<EntradaInventario> getEntradas() {
        return entradaInventarioRepository.findAllConRelaciones();
    }

    // Registra la entrada y aumenta el stock automaticamente HU-11
    @Transactional
    public void save(EntradaInventario entrada, Usuario usuario) {
        if (entrada.getMedicamento() == null || entrada.getMedicamento().getIdMedicamento() == null) {
            throw new IllegalArgumentException("Debe seleccionar un medicamento.");
        }
        var idMedicamento = entrada.getMedicamento().getIdMedicamento();
        if (medicamentoService.getMedicamento(idMedicamento).isEmpty()) {
            throw new IllegalArgumentException("El medicamento seleccionado no existe.");
        }
        //El responsable es el usuario autenticado, no uno escogido en el formulario
        entrada.setUsuario(usuario);
        entradaInventarioRepository.save(entrada);
        medicamentoService.ajustarStockYRegistrar(idMedicamento, entrada.getCantidad(), true,
                "El medicamento ya no existe.", usuario, TipoMovimiento.Entrada,
                "Entrada lote " + entrada.getLote(), entrada.getIdEntrada());
    }
}
