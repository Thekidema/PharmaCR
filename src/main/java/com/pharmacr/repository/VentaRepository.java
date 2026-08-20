package com.pharmacr.repository;

import com.pharmacr.domain.Venta;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

    //El listado muestra usuario.username en cada fila
    @Query("select v from Venta v join fetch v.usuario order by v.fecha desc")
    public List<Venta> findAllConUsuario();

    //HU-18: trae la venta con su vendedor de un solo viaje, para el comprobante
    @Query("select v from Venta v join fetch v.usuario where v.idVenta = :idVenta")
    public Optional<Venta> findByIdConUsuario(@Param("idVenta") Integer idVenta);

    //HU-13: reporte de ventas por rango de fechas
    @Query("select v from Venta v join fetch v.usuario "
            + "where v.fecha >= :desde and v.fecha < :hasta order by v.fecha desc")
    public List<Venta> findPorRango(@Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    @Query("select count(v) from Venta v where v.fecha >= :desde and v.fecha < :hasta")
    public long contarPorRango(@Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    @Query("select sum(v.total) from Venta v where v.fecha >= :desde and v.fecha < :hasta")
    public BigDecimal sumarPorRango(@Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
    @Query("""
           select d.medicamento.nombre, sum(d.cantidad), sum(d.subtotal)
             from DetalleVenta d
            where d.venta.fecha >= :desde and d.venta.fecha < :hasta
            group by d.medicamento.idMedicamento, d.medicamento.nombre
            order by sum(d.cantidad) desc
           """)
    public List<Object[]> findMasVendidos(@Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}
