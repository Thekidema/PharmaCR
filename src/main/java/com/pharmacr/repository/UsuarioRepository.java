package com.pharmacr.repository;

import com.pharmacr.domain.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    //Se crea una consulta derivada para recuperar los registros de la base de datos
    public List<Usuario> findByActivoTrue();

    //Consulta que usa el proceso de autenticacion de Spring Security
    public Optional<Usuario> findByUsernameAndActivoTrue(String username);

    public Optional<Usuario> findByUsername(String username);

    public boolean existsByUsernameOrCorreo(String username, String correo);

    //Recuperacion de clave: busca el usuario a partir del correo registrado
    public Optional<Usuario> findByCorreo(String correo);

    //Consulta JPQL con LEFT JOIN FETCH: trae los usuarios con sus roles
    @Query("select distinct u from Usuario u left join fetch u.roles order by u.idUsuario")
    public List<Usuario> findAllConRoles();

    @Query("select distinct u from Usuario u left join fetch u.roles where u.activo = true order by u.idUsuario")
    public List<Usuario> findActivosConRoles();

    @Query("select distinct u from Usuario u left join fetch u.roles where u.idUsuario = :idUsuario")
    public Optional<Usuario> findByIdConRoles(Integer idUsuario);

    //HU-16: destinatarios del correo de alerta de stock minimo
    @Query("select distinct u from Usuario u join u.roles r where r.rol = :rol and u.activo = true")
    public List<Usuario> findActivosPorRol(String rol);
}
