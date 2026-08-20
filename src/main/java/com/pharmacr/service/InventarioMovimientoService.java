package com.pharmacr.service;

import com.pharmacr.domain.InventarioMovimiento;
import com.pharmacr.domain.Medicamento;
import com.pharmacr.domain.TipoMovimiento;
import com.pharmacr.domain.Usuario;
import com.pharmacr.repository.InventarioMovimientoRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


 //HU-17: registra y consulta la bitacora de movimientos de inventario

@Service
public class InventarioMovimientoService {

    private final InventarioMovimientoRepository movimientoRepository;

    public InventarioMovimientoService(InventarioMovimientoRepository movimientoRepository) {
        this.movimientoRepository = movimientoRepository;
    }


    @Transactional
    public void registrar(Medicamento medicamento, Usuario usuario, TipoMovimiento tipo,
            Integer cantidad, String motivo, Integer idReferencia) {

        var movimiento = new InventarioMovimiento();
        movimiento.setMedicamento(medicamento);
        movimiento.setUsuario(usuario);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setStockResultante(medicamento.getStockActual());
        movimiento.setMotivo(motivo);
        movimiento.setIdReferencia(idReferencia);
        movimiento.setFechaCreacion(LocalDateTime.now());
        movimientoRepository.save(movimiento);
    }

    @Transactional(readOnly = true)
    public List<InventarioMovimiento> buscar(Integer idMedicamento, Integer idUsuario,
            LocalDateTime desde, LocalDateTime hasta) {
        return movimientoRepository.buscar(idMedicamento, idUsuario, desde, hasta);
    }
}
