package com.pharmacr.service;

import com.pharmacr.domain.Rol;
import com.pharmacr.domain.Usuario;
import com.pharmacr.repository.RolRepository;
import com.pharmacr.repository.UsuarioRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UsuarioService {

    // Se enlazan los repositorios y el encriptador de claves
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    //Firebase es opcional: si el equipo no configuro las credenciales
    private final ObjectProvider<FirebaseStorageService> firebaseStorageService;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            ObjectProvider<FirebaseStorageService> firebaseStorageService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.firebaseStorageService = firebaseStorageService;
    }

    @Transactional(readOnly = true)
    public List<Usuario> getUsuarios(boolean activo) {
        if (activo) { //Solo quiero los usuarios activos
            return usuarioRepository.findActivosConRoles();
        }
        return usuarioRepository.findAllConRoles();
    }

    //Recupera un registro de usuario -si existe-
    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuario(Integer idUsuario) {
        return usuarioRepository.findByIdConRoles(idUsuario);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuario(String username) {
        return usuarioRepository.findByUsername(username);
    }
    //Si Usuario trae un idUsuario se actualiza el registro, si no se crea
     
    @Transactional
    public void save(Usuario usuario, MultipartFile imagen, boolean encriptaClave) {
        salvarDatos(usuario, encriptaClave);

        var storage = firebaseStorageService.getIfAvailable();
        if (storage != null && imagen != null && !imagen.isEmpty()) {
            try {
                usuario.setRutaImagen(storage.subirImagen(imagen, "usuario", usuario.getIdUsuario()));
                usuarioRepository.save(usuario);
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo subir la imagen del usuario: " + e.getMessage());
            }
        }
    }

    @Transactional
    public void save(Usuario usuario, boolean encriptaClave) {
        save(usuario, null, encriptaClave);
    }

    private void salvarDatos(Usuario usuario, boolean encriptaClave) {
        if (usuario.getIdUsuario() == null) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        } else {
            var existente = usuarioRepository.findByIdConRoles(usuario.getIdUsuario());
            if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
                existente.ifPresent(u -> usuario.setPassword(u.getPassword()));
            } else if (encriptaClave) {
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            }
            if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
                //Sin roles en el formulario se conservan los actuales, para no borrarlos en silencio
                existente.ifPresent(u -> usuario.setRoles(new HashSet<>(u.getRoles())));
            }
            if (usuario.getRutaImagen() == null) {
                //El formulario no manda la ruta de la foto: se conserva la que ya tenia
                existente.ifPresent(u -> usuario.setRutaImagen(u.getRutaImagen()));
            }
        }
        usuarioRepository.save(usuario);
    }

    //Baja logica del usuario no se elimina fisicamente, se marca como inactivo
    @Transactional
    public void desactivar(Integer idUsuario) {
        var usuario = usuarioRepository.findById(idUsuario);
        if (usuario.isEmpty()) {
            throw new IllegalArgumentException("El usuario con ID " + idUsuario + " no existe!");
        }
        usuario.get().setActivo(false);
        usuarioRepository.save(usuario.get());
    }

    //Gestion de roles HU-03
    @Transactional(readOnly = true)
    public List<Rol> getRoles() {
        return rolRepository.findAll();
    }

    //Recupera los roles seleccionados en el formulario 
    @Transactional(readOnly = true)
    public List<Rol> getRoles(List<Integer> idsRoles) {
        if (idsRoles == null) {
            return new ArrayList<>();
        }
        return rolRepository.findAllById(idsRoles);
    }

    //Devuelve los nombres de los roles que tiene asignados un usuario
    @Transactional(readOnly = true)
    public List<String> getRolesNombres(String username) {
        var roles = new ArrayList<String>();
        usuarioRepository.findByUsername(username).ifPresent(usuario
                -> usuario.getRoles().forEach(rol -> roles.add(rol.getRol())));
        return roles;
    }

    //Devuelve los roles que el usuario ya tiene, con su id, para poder quitarlos
    @Transactional(readOnly = true)
    public List<Rol> getRolesAsignados(String username) {
        return usuarioRepository.findByUsername(username)
                .map(usuario -> List.copyOf(usuario.getRoles()))
                .orElseGet(List::of);
    }

    //Devuelve los roles que al usuario todavia no se le han asignado
    @Transactional(readOnly = true)
    public List<Rol> getRolesDisponibles(String username) {
        var asignados = getRolesNombres(username);
        return rolRepository.findAll().stream()
                .filter(rol -> !asignados.contains(rol.getRol()))
                .toList();
    }

    @Transactional
    public void asignarRol(String username, Integer idRol) {
        var usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("El usuario " + username + " no existe!"));
        var rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new IllegalArgumentException("El rol indicado no existe!"));
        usuario.getRoles().add(rol);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void asignarRolPorNombre(String username, String nombreRol) {
        var rol = rolRepository.findByRol(nombreRol)
                .orElseThrow(() -> new IllegalArgumentException("El rol " + nombreRol + " no existe!"));
        asignarRol(username, rol.getIdRol());
    }

    @Transactional
    public void eliminarRol(String username, Integer idRol) {
        var usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("El usuario " + username + " no existe!"));
        if (usuario.getRoles().size() <= 1) {
            //Un usuario sin roles no podria hacer nada dentro del sistema
            throw new IllegalStateException("El usuario debe conservar al menos un rol");
        }
        usuario.getRoles().removeIf(rol -> rol.getIdRol().equals(idRol));
        usuarioRepository.save(usuario);
    }
}
