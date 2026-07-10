package com.pharmacr.repository;

import com.pharmacr.domain.DetalleVenta;
import com.pharmacr.domain.Venta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Integer> {

    List<DetalleVenta> findByVenta(Venta venta);
}
