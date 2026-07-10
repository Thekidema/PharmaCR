package com.pharmacr.service;

import com.pharmacr.domain.Medicamento;
import com.pharmacr.repository.MedicamentoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicamentoService {

    // Se enlaza el repositorio de medicamento
    private final MedicamentoRepository medicamentoRepository;

    public MedicamentoService(MedicamentoRepository medicamentoRepository) {
        this.medicamentoRepository = medicamentoRepository;
    }

    @Transactional(readOnly = true)
    public List<Medicamento> getMedicamentos(boolean activo) {
        if (activo) {                                               //Solo quiero los medicamentos activos
            return medicamentoRepository.findByActivoTrue();
        }
        return medicamentoRepository.findAll();
    }
                                                           //Recupera un registro de medicamento si este existe

    @Transactional(readOnly = true)
    public Optional<Medicamento> getMedicamento(Integer idMedicamento) {
        return medicamentoRepository.findById(idMedicamento);
    }

    //Búsqueda por nombre o código, solo activos (HU-09)
    @Transactional(readOnly = true)
    public List<Medicamento> buscar(String termino) {
        var medicamentos = medicamentoRepository
                .findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(termino, termino);
        medicamentos.removeIf(m -> !m.isActivo());
        return medicamentos;
    }
                                                                             //Si el Medicamento trae un idMedicamento se actualiza el registro, sino se crea

    @Transactional
    public void save(Medicamento medicamento) {
        medicamentoRepository.save(medicamento);
    }

    //Si el idMedicamento existe, se elimina si no tiene informacion asociada
    @Transactional
    public void delete(Integer idMedicamento) {
        //Se valida que el medicamento exista...
        if (!medicamentoRepository.existsById(idMedicamento)) {
            // hace una excepcion para indicarle al usuario que no se elimino
            throw new IllegalArgumentException("El medicamento con ID " + idMedicamento + " no existe!");
        }
        try {
            medicamentoRepository.deleteById(idMedicamento);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("No se puede eliminar el medicamento, tiene información asociada");
        }
    }

    //inabilitar de medicamentos descontinuados HU-06
    @Transactional
    public void desactivar(Integer idMedicamento) {
        var medicamento = medicamentoRepository.findById(idMedicamento);
        if (medicamento.isEmpty()) {
            throw new IllegalArgumentException("El medicamento con ID " + idMedicamento + " no existe!");
        }
        medicamento.get().setActivo(false);
        medicamentoRepository.save(medicamento.get());
    }
}
