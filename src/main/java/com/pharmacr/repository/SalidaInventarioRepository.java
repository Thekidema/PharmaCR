package com.pharmacr.repository;

import com.pharmacr.domain.SalidaInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalidaInventarioRepository extends JpaRepository<SalidaInventario, Integer> {
}
