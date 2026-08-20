package com.pharmacr.repository;

import com.pharmacr.domain.SalidaInventario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SalidaInventarioRepository extends JpaRepository<SalidaInventario, Integer> {

    @Query("""
           select s from SalidaInventario s
             join fetch s.medicamento
             join fetch s.usuario
            order by s.idSalida desc
           """)
    public List<SalidaInventario> findAllConRelaciones();
}
