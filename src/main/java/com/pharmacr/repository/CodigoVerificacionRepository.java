package com.pharmacr.repository;

import com.pharmacr.domain.CodigoVerificacion;
import com.pharmacr.domain.Usuario;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodigoVerificacionRepository extends JpaRepository<CodigoVerificacion, Integer> {

    //Busca el codigo vigente
    Optional<CodigoVerificacion> findByUsuarioAndCodigoAndUsadoFalseAndFechaExpiracionAfter(
            Usuario usuario, String codigo, LocalDateTime ahora);

    //Codigos previos sin usar del usuario, para invalidarlos al pedir uno nuevo
    List<CodigoVerificacion> findByUsuarioAndUsadoFalse(Usuario usuario);
}
