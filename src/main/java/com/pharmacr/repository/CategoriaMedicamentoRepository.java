package com.pharmacr.repository;

import com.pharmacr.domain.CategoriaMedicamento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaMedicamentoRepository extends JpaRepository<CategoriaMedicamento, Integer> {

    //Se crea una consulta derivada para recuperar los registros de la base de datos
    public List<CategoriaMedicamento> findByActivoTrue();
}
