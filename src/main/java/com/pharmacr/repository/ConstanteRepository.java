package com.pharmacr.repository;

import com.pharmacr.domain.Constante;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConstanteRepository extends JpaRepository<Constante, Integer> {

    public Optional<Constante> findByAtributo(String atributo);
}
