package com.pharmacr.service;

import com.pharmacr.domain.Constante;
import com.pharmacr.repository.ConstanteRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConstanteService {

    // Se enlaza el repositorio de constante
    private final ConstanteRepository constanteRepository;

    public ConstanteService(ConstanteRepository constanteRepository) {
        this.constanteRepository = constanteRepository;
    }

    @Transactional(readOnly = true)
    public List<Constante> getConstantes() {
        return constanteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Constante> getConstante(Integer idConstante) {
        return constanteRepository.findById(idConstante);
    }

    //Devuelve el valor del atributo; null si la constante no esta configurada
    @Transactional(readOnly = true)
    public String valorAtributo(String atributo) {
        return constanteRepository.findByAtributo(atributo)
                .map(Constante::getValor)
                .orElse(null);
    }
    @Transactional(readOnly = true)
    public int valorEntero(String atributo, int porDefecto) {
        var valor = valorAtributo(atributo);
        if (valor == null) {
            return porDefecto;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }

    @Transactional
    public void save(Constante constante) {
        constanteRepository.save(constante);
    }

    @Transactional
    public void delete(Integer idConstante) {
        if (!constanteRepository.existsById(idConstante)) {
            throw new IllegalArgumentException("La constante con ID " + idConstante + " no existe!");
        }
        try {
            constanteRepository.deleteById(idConstante);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("No se puede eliminar la constante, tiene información asociada");
        }
    }
}
