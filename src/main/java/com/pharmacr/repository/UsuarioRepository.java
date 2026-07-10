package com.pharmacr.repository;

import com.pharmacr.domain.Usuario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    //Se crea una consulta derivada para recuperar los registros de la base de datos
    public List<Usuario> findByActivoTrue();
}
