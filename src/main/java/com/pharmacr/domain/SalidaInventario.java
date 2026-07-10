package com.pharmacr.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
@Entity
@Table(name = "salida_inventario")
public class SalidaInventario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSalida;

    @ManyToOne
    @JoinColumn(name = "idMedicamento")
    private Medicamento medicamento;

    @ManyToOne
    @JoinColumn(name = "idUsuario")
    private Usuario usuario;

    @Column(nullable = false, length = 20)
    @NotNull
    @Size(max = 20)
    private String tipo;

    @Column(nullable = false)
    @NotNull
    private Integer cantidad;

    @Column(nullable = false, length = 300)
    @NotNull
    @Size(max = 300)
    private String motivo;
}
