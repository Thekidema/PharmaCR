package com.pharmacr.repository;

import com.pharmacr.domain.Rol;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    //Se usa para asignarle un rol a un usuario a partir del nombre del rol
    public Optional<Rol> findByRol(String rol);
}
