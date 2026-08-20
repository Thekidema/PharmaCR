package com.pharmacr.service;

import com.pharmacr.domain.Ruta;
import com.pharmacr.repository.RutaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RutaService {

    // Se enlaza el repositorio de ruta
    private final RutaRepository rutaRepository;

    public RutaService(RutaRepository rutaRepository) {
        this.rutaRepository = rutaRepository;
    }

    //Entrega las rutas que SecurityConfig usa para armar el filtro de autorizacion
    @Transactional(readOnly = true)
    public List<Ruta> getRutas() {
        return rutaRepository.findAllByOrderByRequiereRolAscIdRutaAsc();
    }
}
