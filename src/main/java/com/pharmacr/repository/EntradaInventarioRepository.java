package com.pharmacr.repository;

import com.pharmacr.domain.EntradaInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntradaInventarioRepository extends JpaRepository<EntradaInventario, Integer> {
}
