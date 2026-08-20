package com.pharmacr.service;

import com.pharmacr.domain.DetalleVenta;
import com.pharmacr.domain.EstadoVenta;
import com.pharmacr.domain.Item;
import com.pharmacr.domain.TipoMovimiento;
import com.pharmacr.domain.Usuario;
import com.pharmacr.domain.Venta;
import com.pharmacr.repository.DetalleVentaRepository;
import com.pharmacr.repository.VentaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final MedicamentoService medicamentoService;

    public VentaService(VentaRepository ventaRepository, DetalleVentaRepository detalleVentaRepository,
            MedicamentoService medicamentoService) {
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.medicamentoService = medicamentoService;
    }

    @Transactional(readOnly = true)
    public List<Venta> getVentas() {
        return ventaRepository.findAllConUsuario();
    }

    @Transactional(readOnly = true)
    public Optional<Venta> getVenta(Integer idVenta) {
        return ventaRepository.findById(idVenta);
    }

    @Transactional(readOnly = true)
    public List<DetalleVenta> getDetalles(Venta venta) {
        return detalleVentaRepository.findByVenta(venta);
    }

    // HU-13: reporte de ventas por rango de fechas
    @Transactional(readOnly = true)
    public List<Venta> getVentasPorRango(LocalDateTime desde, LocalDateTime hasta) {
        return ventaRepository.findPorRango(desde, hasta);
    }

    @Transactional(readOnly = true)
    public long contarPorRango(LocalDateTime desde, LocalDateTime hasta) {
        return ventaRepository.contarPorRango(desde, hasta);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalPorRango(LocalDateTime desde, LocalDateTime hasta) {
        var total = ventaRepository.sumarPorRango(desde, hasta);
        return total == null ? BigDecimal.ZERO : total;
    }

    @Transactional(readOnly = true)
    public List<Object[]> getMasVendidos(LocalDateTime desde, LocalDateTime hasta) {
        return ventaRepository.findMasVendidos(desde, hasta);
    }

    //HU-20: cuenta las ventas del dia en la consulta, sin traerlas todas a memoria
    @Transactional(readOnly = true)
    public long contarVentasDelDia() {
        var hoy = LocalDate.now();
        return ventaRepository.contarPorRango(hoy.atStartOfDay(), hoy.plusDays(1).atStartOfDay());
    }

    @Transactional(readOnly = true)
    public BigDecimal totalVentasDelDia() {
        var hoy = LocalDate.now();
        var total = ventaRepository.sumarPorRango(hoy.atStartOfDay(), hoy.plusDays(1).atStartOfDay());
        return total == null ? BigDecimal.ZERO : total;
    }

     // HU-08: registra la venta completa con todas sus lineas, descuenta elinventario de cada medicamento y deja el rastro en la bitacora
    public Venta registrar(Usuario usuario, List<Item> items) {
        return registrar(usuario, items, null);
    }

    //Igual que registrar(usuario, items), pero permite indicar el correo delcliente para poder enviarle la factura despues HU-18
    @Transactional
    public Venta registrar(Usuario usuario, List<Item> items, String correoCliente) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Debe agregar al menos un medicamento a la venta.");
        }

        //Primero se valida todo el carrito: asi no se descuenta stock de una linea
        
       
        for (Item item : items) {
            var med = medicamentoService.getMedicamento(item.getIdMedicamento())
                    .orElseThrow(() -> new IllegalArgumentException("El medicamento seleccionado no existe."));
            medicamentoService.validarDisponibilidad(med, item.getCantidad());
        }

        var total = Item.sumarTotal(items);

        var venta = new Venta();
        venta.setUsuario(usuario);
        venta.setFecha(LocalDateTime.now());
        venta.setTotal(total);
        venta.setEstado(EstadoVenta.Completada);
        venta.setCorreoCliente(correoCliente);
        ventaRepository.save(venta);

        for (Item item : items) {
            var med = medicamentoService.getMedicamento(item.getIdMedicamento()).orElseThrow();

            var detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setMedicamento(med);
            detalle.setCantidad(item.getCantidad());
            //Precio historico: el que tenia el medicamento cuando se hizo la venta
            detalle.setPrecioUnitario(item.getPrecio());
            detalle.setSubtotal(item.getSubtotal());
            detalleVentaRepository.save(detalle);

    
            medicamentoService.ajustarStockYRegistrar(item.getIdMedicamento(), item.getCantidad(), false,
                    "No hay stock suficiente de " + med.getNombre()
                            + ": el inventario cambió mientras se procesaba la venta. Intente de nuevo.",
                    usuario, TipoMovimiento.Venta, "Venta #" + venta.getIdVenta(), venta.getIdVenta());
        }

        return venta;
    }

    //Anula una venta ya registrada: repone el stock de cada linea, deja rastro en la bitacora como Ajuste y marca la venta como Anulada
     
    @Transactional
    public void anular(Integer idVenta, Usuario usuario) {
        var venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new IllegalArgumentException("La venta no existe."));
        if (venta.getEstado() == EstadoVenta.Anulada) {
            throw new IllegalStateException("La venta ya está anulada.");
        }

        for (DetalleVenta detalle : detalleVentaRepository.findByVenta(venta)) {
            var idMedicamento = detalle.getMedicamento().getIdMedicamento();
            medicamentoService.ajustarStockYRegistrar(idMedicamento, detalle.getCantidad(), true,
                    "El medicamento ya no existe.", usuario, TipoMovimiento.Ajuste,
                    "Anulación venta #" + venta.getIdVenta(), venta.getIdVenta());
        }

        venta.setEstado(EstadoVenta.Anulada);
        ventaRepository.save(venta);
    }
}
