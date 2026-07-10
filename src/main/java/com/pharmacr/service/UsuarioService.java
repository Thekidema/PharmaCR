package com.pharmacr.service;

import com.pharmacr.domain.Usuario;
import com.pharmacr.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    // Se enlaza el repositorio de usuario
    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Usuario> getUsuarios(boolean activo) {
        if (activo) { //Solo quiero los usuarios activos
            return usuarioRepository.findByActivoTrue();
        }
        return usuarioRepository.findAll();
    }
    //Recupera un registro de usuario -si existe-

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuario(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario);
    }
    //Si Usuario trae un idUsuario... se actualiza el registro, sino se crea

    @Transactional
    public void save(Usuario usuario) {
        // El formulario aún no administra roles (HU-03): al modificar se conservan los actuales
        if (usuario.getIdUsuario() != null && usuario.getRoles() == null) {
            var existente = usuarioRepository.findById(usuario.getIdUsuario());
            if (existente.isPresent()) {
                usuario.setRoles(existente.get().getRoles());
            }
        }
        usuarioRepository.save(usuario);
    }

    //Si idUsuario existe, se elimina... si no tiene información asociada
    @Transactional
    public void delete(Integer idUsuario) {
        //Se valida que el usuario exista...
        if (!usuarioRepository.existsById(idUsuario)) {
            //Se lanza una excepción para indicarle al usuario que no se eliminó
            throw new IllegalArgumentException("El usuario con ID " + idUsuario + " no existe!");
        }
        try {
            usuarioRepository.deleteById(idUsuario);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("No se puede eliminar el usuario, tiene información asociada");
        }
    }
}
