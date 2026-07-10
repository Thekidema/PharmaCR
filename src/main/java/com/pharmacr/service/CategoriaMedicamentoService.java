package com.pharmacr.service;

import com.pharmacr.domain.CategoriaMedicamento;
import com.pharmacr.repository.CategoriaMedicamentoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaMedicamentoService {

    // Solo se enlaza el repositorio de categoría de medicamento
    private final CategoriaMedicamentoRepository categoriaMedicamentoRepository;

    public CategoriaMedicamentoService(CategoriaMedicamentoRepository categoriaMedicamentoRepository) {
        this.categoriaMedicamentoRepository = categoriaMedicamentoRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaMedicamento> getCategorias(boolean activo) {
        if (activo) { //Solo quiero las categorias activas
            return categoriaMedicamentoRepository.findByActivoTrue();
        }
        return categoriaMedicamentoRepository.findAll();
    }
}
