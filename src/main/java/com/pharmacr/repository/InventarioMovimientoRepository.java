package com.pharmacr.repository;

import com.pharmacr.domain.InventarioMovimiento;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventarioMovimientoRepository extends JpaRepository<InventarioMovimiento, Integer> {

    //HU-17: historial filtrable por medicamento, usuario y rango de fechas
    @Query("""
           select m from InventarioMovimiento m
             join fetch m.medicamento med
             join fetch m.usuario u
            where (:idMedicamento is null or med.idMedicamento = :idMedicamento)
              and (:idUsuario     is null or u.idUsuario     = :idUsuario)
              and (:desde         is null or m.fechaCreacion >= :desde)
              and (:hasta         is null or m.fechaCreacion <= :hasta)
            order by m.fechaCreacion desc
           """)
    public List<InventarioMovimiento> buscar(@Param("idMedicamento") Integer idMedicamento,
            @Param("idUsuario") Integer idUsuario,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}
