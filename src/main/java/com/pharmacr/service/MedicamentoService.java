package com.pharmacr.service;

import com.pharmacr.domain.EntradaInventario;
import com.pharmacr.domain.Medicamento;
import com.pharmacr.domain.TipoMovimiento;
import com.pharmacr.domain.Usuario;
import com.pharmacr.repository.EntradaInventarioRepository;
import com.pharmacr.repository.MedicamentoRepository;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicamentoService {

    // Se enlazan los repositorios de medicamento y de entradas (para los lotes)
    private final MedicamentoRepository medicamentoRepository;
    private final EntradaInventarioRepository entradaInventarioRepository;
    private final AlertaService alertaService;
    private final InventarioMovimientoService movimientoService;

    public MedicamentoService(MedicamentoRepository medicamentoRepository,
            EntradaInventarioRepository entradaInventarioRepository,
            AlertaService alertaService, InventarioMovimientoService movimientoService) {
        this.medicamentoRepository = medicamentoRepository;
        this.entradaInventarioRepository = entradaInventarioRepository;
        this.alertaService = alertaService;
        this.movimientoService = movimientoService;
    }

    @Transactional(readOnly = true)
    public List<Medicamento> getMedicamentos(boolean activo) {
        if (activo) {                                               //Solo quiero los medicamentos activos
            return medicamentoRepository.findByActivoTrue();
        }
        return medicamentoRepository.findAll();
    }
                                                           //Recupera un registro de medicamento si este existe

    @Transactional(readOnly = true)
    public Optional<Medicamento> getMedicamento(Integer idMedicamento) {
        return medicamentoRepository.findById(idMedicamento);
    }

    //Busqueda por nombre, codigo o categoria, solo activos (HU-09)
    @Transactional(readOnly = true)
    public List<Medicamento> buscar(String termino) {
        if (termino == null || termino.isBlank()) {
            return medicamentoRepository.findByActivoTrue();
        }
        return medicamentoRepository.buscarActivos(termino.trim());
    }

    //HU-14: medicamentos que llegaron o quedaron por debajo del stock minimo
    @Transactional(readOnly = true)
    public List<Medicamento> getBajoMinimo() {
        return medicamentoRepository.findBajoMinimo();
    }

    @Transactional(readOnly = true)
    public long contarActivos() {
        return medicamentoRepository.contarActivos();
    }

    
     // HU-15: lotes que vencen dentro de los proximos dias
  
    @Transactional(readOnly = true)
    public List<EntradaInventario> getLotesPorVencer(Integer dias) {
        var hoy = LocalDate.now();
        return entradaInventarioRepository.findPorVencer(hoy, hoy.plusDays(dias));
    }

    //HU-07: lotes de un medicamento ordenados por vencimiento, el mas proximo primero
    @Transactional(readOnly = true)
    public List<EntradaInventario> getLotes(Integer idMedicamento) {
        return entradaInventarioRepository.findLotesPorMedicamento(idMedicamento);
    }

    // HU-07: lotes agrupados por medicamento, cada grupo ordenado por fecha de vencimiento
    @Transactional(readOnly = true)
    public Map<Integer, List<EntradaInventario>> getLotesAgrupados() {
        return entradaInventarioRepository.findTodosLosLotes().stream()
                .collect(Collectors.groupingBy(e -> e.getMedicamento().getIdMedicamento(),
                        LinkedHashMap::new, Collectors.toList()));
    }
                                                                             //Si el Medicamento trae un idMedicamento se actualiza el registro, sino se crea

    @Transactional
    public void save(Medicamento medicamento) {
        medicamentoRepository.save(medicamento);
    }

    //inabilitar de medicamentos descontinuados HU-06
    @Transactional
    public void desactivar(Integer idMedicamento) {
        var medicamento = medicamentoRepository.findById(idMedicamento);
        if (medicamento.isEmpty()) {
            throw new IllegalArgumentException("El medicamento con ID " + idMedicamento + " no existe!");
        }
        medicamento.get().setActivo(false);
        medicamentoRepository.save(medicamento.get());
    }

    //Valida que el medicamento este activo y tenga stock suficiente

    public void validarDisponibilidad(Medicamento medicamento, int cantidadSolicitada) {
        if (!medicamento.isActivo()) {
            throw new IllegalArgumentException("El medicamento " + medicamento.getNombre() + " está desactivado.");
        }
        if (medicamento.getStockActual() == null || cantidadSolicitada > medicamento.getStockActual()) {
            throw new IllegalArgumentException("No hay stock suficiente de " + medicamento.getNombre()
                    + " (disponible: " + medicamento.getStockActual() + ", solicitado: " + cantidadSolicitada + ").");
        }
    }

    /**
     * Ajusta el stock de un medicamento de forma atomica (ver
     * MedicamentoRepository.descontarStock/aumentarStock), revisa si corresponde
     * generar o cerrar una alerta de stock minimo, y deja el rastro en la
     * bitacora de movimientos. Venta, EntradaInventario y SalidaInventario usan
     * este mismo metodo en vez de repetir esta secuencia de tres pasos cada una.
     */
    @Transactional
    public Medicamento ajustarStockYRegistrar(Integer idMedicamento, Integer cantidad, boolean aumentar,
            String mensajeSiInsuficiente, Usuario usuario, TipoMovimiento tipo, String motivo, Integer idReferencia) {
        int filas = aumentar
                ? medicamentoRepository.aumentarStock(idMedicamento, cantidad)
                : medicamentoRepository.descontarStock(idMedicamento, cantidad);
        if (filas == 0) {
            throw new IllegalArgumentException(mensajeSiInsuficiente);
        }
        var medicamento = medicamentoRepository.findById(idMedicamento).orElseThrow();
        alertaService.revisar(medicamento);
        movimientoService.registrar(medicamento, usuario, tipo, cantidad, motivo, idReferencia);
        return medicamento;
    }
}
