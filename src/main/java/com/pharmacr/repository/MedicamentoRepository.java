package com.pharmacr.repository;

import com.pharmacr.domain.Medicamento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Integer> {

    @Modifying(clearAutomatically = true)
    @Query("update Medicamento m set m.stockActual = m.stockActual - :cantidad "
            + "where m.idMedicamento = :id and m.stockActual >= :cantidad")
    int descontarStock(@Param("id") Integer id, @Param("cantidad") Integer cantidad);

    @Modifying(clearAutomatically = true)
    @Query("update Medicamento m set m.stockActual = m.stockActual + :cantidad "
            + "where m.idMedicamento = :id")
    int aumentarStock(@Param("id") Integer id, @Param("cantidad") Integer cantidad);

    //Se crea una consulta derivada para recuperar los registros de la base de datos
    public List<Medicamento> findByActivoTrue();

    // HU-09: busqueda por nombre, codigo o categoria, unicamente entre los  medicamentos activos


    @Query("""
           select m from Medicamento m
             join fetch m.categoria c
            where m.activo = true
              and (lower(m.nombre)  like lower(concat('%', :termino, '%'))
                or lower(m.codigo)  like lower(concat('%', :termino, '%'))
                or lower(c.nombre)  like lower(concat('%', :termino, '%')))
            order by m.nombre asc
           """)
    public List<Medicamento> buscarActivos(@Param("termino") String termino);

    //HU-14
    @Query("select m from Medicamento m where m.activo = true and m.stockActual <= m.stockMinimo "
            + "order by m.stockActual asc")
    public List<Medicamento> findBajoMinimo();

    @Query("select count(m) from Medicamento m where m.activo = true")
    public long contarActivos();
}
