package com.pharmacr.service;

import com.pharmacr.domain.Proveedor;
import com.pharmacr.repository.ProveedorRepository;
import java.util.List;
import java.util.Optional;
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
    //Recupera un registro de proveedor -si

    @Transactional(readOnly = true)
    public Optional<Proveedor> getProveedor(Integer idProveedor) {
        return proveedorRepository.findById(idProveedor);
    }
    //Si Proveedor trae un idProveedor... se actualiza el registro, sino se crea

    @Transactional
    public void save(Proveedor proveedor) {
        proveedorRepository.save(proveedor);
    }

    //Baja logica del proveedor: no se elimina fisicamente, se marca como inactivo
    @Transactional
    public void desactivar(Integer idProveedor) {
        var proveedor = proveedorRepository.findById(idProveedor);
        if (proveedor.isEmpty()) {
            throw new IllegalArgumentException("El proveedor con ID " + idProveedor + " no existe!");
        }
        proveedor.get().setActivo(false);
        proveedorRepository.save(proveedor.get());
    }
}
