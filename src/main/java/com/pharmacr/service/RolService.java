package com.pharmacr.service;

import com.pharmacr.domain.Rol;
import com.pharmacr.repository.RolRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RolService {

    // Se enlaza el repositorio de rol
    private final RolRepository rolRepository;

    public RolService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Transactional(readOnly = true)
    public List<Rol> getRoles() {
        return rolRepository.findAll();
    }

    //Recupera los roles seleccionados en el formulario (checkboxes)
    @Transactional(readOnly = true)
    public List<Rol> getRoles(List<Integer> idsRoles) {
        if (idsRoles == null) {
            return new ArrayList<>();
        }
        return rolRepository.findAllById(idsRoles);
    }
}
