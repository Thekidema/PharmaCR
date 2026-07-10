package com.pharmacr.repository;

import com.pharmacr.domain.Medicamento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Integer> {

                //Se crea una consulta derivada para recuperar los registros de la base de datos
    public List<Medicamento> findByActivoTrue();

                //Consulta derivada para la búsqueda por nombre o codigo para HU-09
    public List<Medicamento> findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(String nombre, String codigo);
}
