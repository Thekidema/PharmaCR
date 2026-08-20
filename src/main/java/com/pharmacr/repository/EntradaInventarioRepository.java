package com.pharmacr.repository;

import com.pharmacr.domain.EntradaInventario;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EntradaInventarioRepository extends JpaRepository<EntradaInventario, Integer> {

    @Query("""
           select e from EntradaInventario e
             join fetch e.proveedor
             join fetch e.medicamento
             join fetch e.usuario
            order by e.idEntrada desc
           """)
    public List<EntradaInventario> findAllConRelaciones();

    // HU-15: lotes que vencen dentro del periodo elegido
    @Query("""
           select e from EntradaInventario e
             join fetch e.medicamento
            where e.fechaVencimiento >= :hoy and e.fechaVencimiento <= :limite
            order by e.fechaVencimiento asc
           """)
    public List<EntradaInventario> findPorVencer(@Param("hoy") LocalDate hoy,
            @Param("limite") LocalDate limite);

    //HU-07: lotes de un medicamento, del vencimiento mas proximo al mas lejano
    @Query("select e from EntradaInventario e where e.medicamento.idMedicamento = :idMedicamento "
            + "order by e.fechaVencimiento asc")
    public List<EntradaInventario> findLotesPorMedicamento(@Param("idMedicamento") Integer idMedicamento);

    //HU-07: todos los lotes ya ordenados por vencimiento, para agruparlos por
    //medicamento en el servicio en vez de recorrerlos dentro de la plantilla
    @Query("""
           select e from EntradaInventario e
             join fetch e.medicamento
            order by e.medicamento.idMedicamento asc, e.fechaVencimiento asc
           """)
    public List<EntradaInventario> findTodosLosLotes();
}
