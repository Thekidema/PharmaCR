package com.pharmacr.repository;

import com.pharmacr.domain.Alerta;
import com.pharmacr.domain.Medicamento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Integer> {

    //Se crea una consulta derivada para recuperar los registros de la base de datos
    public List<Alerta> findByActivoTrue();

    //Consulta derivada para las alertas activas de un medicamento
    public List<Alerta> findByMedicamentoAndActivoTrue(Medicamento medicamento);
}
