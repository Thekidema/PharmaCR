package com.pharmacr.service;

import com.pharmacr.domain.DetalleVenta;
import com.pharmacr.domain.Usuario;
import com.pharmacr.domain.Venta;
import com.pharmacr.repository.DetalleVentaRepository;
import com.pharmacr.repository.MedicamentoRepository;
import com.pharmacr.repository.VentaRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final AlertaService alertaService;

    public VentaService(VentaRepository ventaRepository, DetalleVentaRepository detalleVentaRepository,
            MedicamentoRepository medicamentoRepository, AlertaService alertaService) {
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.alertaService = alertaService;
    }

    @Transactional(readOnly = true)
    public List<Venta> getVentas() {
        return ventaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Venta> getVenta(Integer idVenta) {
        return ventaRepository.findById(idVenta);
    }

    @Transactional(readOnly = true)
    public List<DetalleVenta> getDetalles(Venta venta) {
        return detalleVentaRepository.findByVenta(venta);
    }

    // Registra la venta, descuenta inventario y no permite vender más del stock (HU-08)
    @Transactional
    public void registrar(Usuario usuario, Integer idMedicamento, Integer cantidad) {
        var medicamento = medicamentoRepository.findById(idMedicamento);
        if (medicamento.isEmpty()) {
            throw new IllegalArgumentException("El medicamento seleccionado no existe.");
        }
        var med = medicamento.get();
        if (!med.isActivo()) {
            throw new IllegalArgumentException("El medicamento " + med.getNombre() + " está desactivado.");
        }
        if (cantidad > med.getStockActual()) {
            throw new IllegalArgumentException("No hay stock suficiente de " + med.getNombre()
                    + " (stock: " + med.getStockActual() + ", solicitado: " + cantidad + ").");
        }

        var subtotal = med.getPrecio().multiply(BigDecimal.valueOf(cantidad));

        var venta = new Venta();
        venta.setUsuario(usuario);
        venta.setFecha(LocalDateTime.now());
        venta.setTotal(subtotal);
        venta.setEstado("Completada");
        ventaRepository.save(venta);

        var detalle = new DetalleVenta();
        detalle.setVenta(venta);
        detalle.setMedicamento(med);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(med.getPrecio());
        detalle.setSubtotal(subtotal);
        detalleVentaRepository.save(detalle);

        med.setStockActual(med.getStockActual() - cantidad);
        medicamentoRepository.save(med);
        alertaService.revisar(med);
    }
}
