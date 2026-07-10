package com.pharmacr.service;

import com.pharmacr.domain.Proveedor;
import com.pharmacr.repository.ProveedorRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProveedorService {

    // Se enlaza el repositorio de proveedor
    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    @Transactional(readOnly = true)
    public List<Proveedor> getProveedores(boolean activo) {
        if (activo) { //Solo quiero los proveedores activos
            return proveedorRepository.findByActivoTrue();
        }
        return proveedorRepository.findAll();
    }
    //Recupera un registro de proveedor -si existe-

    @Transactional(readOnly = true)
    public Optional<Proveedor> getProveedor(Integer idProveedor) {
        return proveedorRepository.findById(idProveedor);
    }
    //Si Proveedor trae un idProveedor... se actualiza el registro, sino se crea

    @Transactional
    public void save(Proveedor proveedor) {
        proveedorRepository.save(proveedor);
    }

    //Si idProveedor existe, se elimina... si no tiene información asociada
    @Transactional
    public void delete(Integer idProveedor) {
        //Se valida que el proveedor exista...
        if (!proveedorRepository.existsById(idProveedor)) {
            //Se lanza una excepción para indicarle al usuario que no se eliminó
            throw new IllegalArgumentException("El proveedor con ID " + idProveedor + " no existe!");
        }
        try {
            proveedorRepository.deleteById(idProveedor);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("No se puede eliminar el proveedor, tiene información asociada");
        }
    }
}
